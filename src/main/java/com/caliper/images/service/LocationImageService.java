package com.caliper.images.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.caliper.images.api.FacebookImageAPI;
import com.caliper.images.api.GmbImageAPI;
import com.caliper.images.dto.request.ImageFilterRequest;
import com.caliper.images.dto.request.UploadImageRequest;
import com.caliper.images.dto.response.ImageDataPageResponse;
import com.caliper.images.dto.response.ImageDataResponse;
import com.caliper.images.entity.LocationImage;
import com.caliper.images.entity.LocationImageMap;
import com.caliper.images.repository.LocationImageMapRepository;
import com.caliper.images.repository.LocationImageRepository;

import com.caliper.location.gmb.service.GMBSessionFactory;
import com.caliper.post.dto.Response.CaliperResponse;
import com.caliper.utils.GoogleBucket;
import com.caliper.utils.exception.customException.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.services.mybusiness.v4.MyBusiness;


@Service
public class LocationImageService {

    private static final Logger log = LoggerFactory.getLogger(LocationImageService.class);

    private static final String GCS_BUCKET_NAME = "caliper-test-bucket";

    private static final int DIRECT_UPLOAD_THRESHOLD = 5;

    private static final int MAX_IMAGES_PER_UPLOAD = 10;


    @Autowired
    private LocationImageRepository locationImageRepository;

    @Autowired
    private LocationImageMapRepository locationImageMapRepository;

    @Autowired
    private GoogleBucket googleBucket;

    @Autowired
    private GmbImageAPI gmbImageAPI;

    @Autowired
    private FacebookImageAPI facebookImageAPI;

    @Autowired

    private GMBSessionFactory gmbSessionFactory;

    // Self-injection so @Transactional on persistImageAndMaps() is honoured
    // when called from within this class (Spring proxy is bypassed otherwise).
    @Autowired
    @Lazy
    private LocationImageService self;

    // -----------------------------------------------------------------------
    // UPLOAD IMAGE
    // -----------------------------------------------------------------------

    // NOT @Transactional — external I/O (GCS + platform API) must not hold a DB connection.
    public CaliperResponse uploadImage(UploadImageRequest req, List<MultipartFile> imageFiles) throws IOException {
        validate(req, imageFiles);

        // Each file is uploaded/persisted independently — one file failing (e.g. a
        // GCS error) doesn't block or roll back the others.
        List<LocationImage> savedImages = new ArrayList<>();
        for (MultipartFile imageFile : imageFiles) {
            File tempFile = null;
            try {
                tempFile = googleBucket.convertMultiPartFile(imageFile);
                String gcsUrl = googleBucket.uploadToBucket(GCS_BUCKET_NAME, tempFile);
                if (gcsUrl == null) {
                    log.error("GCS upload returned null url for client {} file {}",
                            req.getClientId(), imageFile.getOriginalFilename());
                    continue;
                }
                req.setImageUrl(gcsUrl);

                // Persist to DB in a short, dedicated transaction that commits immediately.
                savedImages.add(self.persistImageAndMaps(req));
            } catch (Exception e) {
                log.error("Upload failed for client {} file {}: {}",
                        req.getClientId(), imageFile.getOriginalFilename(), e.getMessage(), e);
            } finally {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }

        // Upload to platform directly if within threshold; otherwise the background
        // task picks up the status=submit records (GMB only).
        if (req.getDealerId().size() <= DIRECT_UPLOAD_THRESHOLD) {
            for (LocationImage savedImage : savedImages) {
                if (LocationImage.PLATFORM_GMB.equals(req.getPlatform())) {
                    deployDirectlyGmb(savedImage, req.getClientId(), req.getDealerId());
                } else if (LocationImage.PLATFORM_FACEBOOK.equals(req.getPlatform())) {
                    deployDirectlyFacebook(savedImage, req.getClientId(), req.getDealerId());
                }
            }
        }

        int failedCount = imageFiles.size() - savedImages.size();
        if (savedImages.isEmpty()) {
            return new CaliperResponse(req.getClientId(), "Failed", "Failed to upload image(s). Please try again.");
        }
        String message = failedCount == 0
                ? "Uploaded " + savedImages.size() + " image(s)"
                : "Uploaded " + savedImages.size() + " image(s), " + failedCount + " failed";
        return new CaliperResponse(req.getClientId(), "Successful", message);
    }

    // Neither GMB nor Facebook recognizes "LOGO" as a real media category - the
    // business's logo/profile photo is always reported back as "PROFILE" by both.
    // Normalize here so a Caliper-side LOGO upload dedupes/replaces against the
    // same slot GMB fetch reports, instead of accumulating a separate row.
    private String normalizeCategory(String category) {
        return LocationImage.CATEGORY_LOGO.equalsIgnoreCase(category)
                ? LocationImage.CATEGORY_PROFILE
                : category;
    }

    // Public so the Spring CGLIB proxy applies @Transactional. Internal use only.
    @Transactional
    public LocationImage persistImageAndMaps(UploadImageRequest req) {
        String category = normalizeCategory(req.getImageCategory());

        LocationImage image = LocationImage.builder()
                .clientId(req.getClientId())
                .imageUrl(req.getImageUrl())
                .imageCategory(category)
                .imageFormat(req.getImageFormat())
                .description(req.getDescription())
                .label(req.getLabel())
                .createdBy(req.getCreatedBy())
                .createdDate(req.getCreatedDate() != null ? req.getCreatedDate() : new Date())
                .status(LocationImage.STATUS_SUBMIT)
                .comment(req.getComment())
                .platform(req.getPlatform())
                .build();

        LocationImage savedImage = locationImageRepository.save(image);

        Date now = new Date();
        List<LocationImageMap> locationMaps = new ArrayList<>();
        for (String dealerId : req.getDealerId()) {
            if (isSingletonCategory(category)) {
                replaceExisting(dealerId, category, req.getPlatform());
            }
            locationMaps.add(LocationImageMap.builder()
                    .imageId(savedImage.getImageId())
                    .dealerId(dealerId)
                    .clientId(req.getClientId())
                    .status(LocationImage.STATUS_SUBMIT)
                    .createdDate(now)
                    .consoleImageId("-1")
                    .build());
        }

        locationImageMapRepository.saveAll(locationMaps);
        return savedImage;
    }

    // Singleton categories (LOGO/COVER/PROFILE) allow only one active image per
    // dealer per platform: delete the dealer's prior map row(s) for this
    // category+platform, and the parent location_image row too once no other
    // map row still references it. Scoped by platform because COVER/PROFILE
    // are also valid Facebook categories sharing the same dealerId.
    private void replaceExisting(String dealerId, String category, String platform) {
        List<LocationImageMap> existing = locationImageMapRepository.findAllByDealerIdAndImageCategoryAndPlatform(dealerId, category, platform);
        deleteMapsAndOrphanedImages(existing);
    }

    private void deleteMapsAndOrphanedImages(List<LocationImageMap> toDelete) {
        if (toDelete.isEmpty()) {
            return;
        }
        locationImageMapRepository.deleteAll(toDelete);
        toDelete.stream()
                .map(LocationImageMap::getImageId)
                .distinct()
                .filter(imageId -> locationImageMapRepository.findByImageId(imageId).isEmpty())
                .forEach(locationImageRepository::deleteById);
    }

    // Each dealer is attempted independently so one failure does not block others.
    // Called outside any transaction — no DB connection held during GMB API I/O.
    private void deployDirectlyGmb(LocationImage savedImage, String clientId, List<String> dealerIds) {
        MyBusiness business;
        try {
            business = gmbSessionFactory.getGMBSession(clientId);
        } catch (Exception e) {
            System.err.println("LocationImageService :: deployDirectlyGmb :: failed to get GMB session: " + e.getMessage());
            return;
        }

        for (String dealerId : dealerIds) {
            try {
                System.out.println("LocationImageService :: deployDirectlyGmb :: uploading imageId="
                        + savedImage.getImageId() + " dealerId=" + dealerId);

                String imageName = gmbImageAPI.uploadImageToGMB(clientId, business, savedImage, dealerId);
                int lastSlash = imageName.lastIndexOf("/");
                String consoleImageId = imageName.substring(lastSlash + 1);

                locationImageMapRepository.updateConsoleImageIdAndStatusByImageIdAndDealerId(
                        consoleImageId, LocationImage.STATUS_DEPLOYED, dealerId, savedImage.getImageId());

                System.out.println("LocationImageService :: deployDirectlyGmb :: deployed dealerId=" + dealerId
                        + " consoleImageId=" + consoleImageId);

            } catch (Exception e) {
                System.err.println("LocationImageService :: deployDirectlyGmb :: failed for dealerId=" + dealerId
                        + " : " + e.getMessage());
                locationImageMapRepository.updateConsoleImageIdAndStatusByImageIdAndDealerId(
                        "-1", LocationImage.STATUS_ERROR, dealerId, savedImage.getImageId());
            }
        }
    }

    private void deployDirectlyFacebook(LocationImage savedImage, String clientId, List<String> dealerIds) {
        for (String dealerId : dealerIds) {
            try {
                System.out.println("LocationImageService :: deployDirectlyFacebook :: uploading imageId="
                        + savedImage.getImageId() + " dealerId=" + dealerId);

                String photoId = facebookImageAPI.uploadImageToFacebook(clientId, savedImage, dealerId);

                locationImageMapRepository.updateConsoleImageIdAndStatusByImageIdAndDealerId(
                        photoId, LocationImage.STATUS_DEPLOYED, dealerId, savedImage.getImageId());

                System.out.println("LocationImageService :: deployDirectlyFacebook :: deployed dealerId=" + dealerId
                        + " photoId=" + photoId);

            } catch (Exception e) {
                System.err.println("LocationImageService :: deployDirectlyFacebook :: failed for dealerId=" + dealerId
                        + " : " + e.getMessage());
                locationImageMapRepository.updateConsoleImageIdAndStatusByImageIdAndDealerId(
                        "-1", LocationImage.STATUS_ERROR, dealerId, savedImage.getImageId());
            }
        }
    }

    // -----------------------------------------------------------------------
    // SINGLETON CATEGORIES (LOGO / COVER / PROFILE) — only one active per dealer
    // -----------------------------------------------------------------------

    public boolean isSingletonCategory(String category) {
        return LocationImage.SINGLETON_CATEGORIES.contains(category);
    }

    public Optional<LocationImageMap> getCurrentImage(String dealerId, String category) {
        return locationImageMapRepository.findByDealerIdAndStatusAndImageCategory(
                dealerId, LocationImage.STATUS_DEPLOYED, category);
    }

    // Everything except superseded rows — an in-flight (submit/processing) or
    // failed (error) upload should still be visible to the caller, not just
    // confirmed-deployed media, so a just-uploaded image never looks like it vanished.
    public List<LocationImageMap> getVisibleMediaForDealer(String dealerId) {
        return locationImageMapRepository.findAllByDealerIdAndStatusNotWithImage(dealerId, LocationImage.STATUS_SUPERSEDED);
    }

    // -----------------------------------------------------------------------
    // FETCH FROM GMB (INBOUND)
    // -----------------------------------------------------------------------

    // Keys off the GMB-assigned media resource id, so this also catches media
    // previously uploaded from Caliper's own UI (which stores that same id as
    // consoleImageId) — not just media imported by a prior fetch run.
    public boolean alreadyImported(String dealerId, String consoleImageId) {
        return locationImageMapRepository.existsByDealerIdAndConsoleImageId(dealerId, consoleImageId);
    }

    @Transactional
    public void persistFetchedGmbMedia(LocationImage image, LocationImageMap map) {
        if (isSingletonCategory(image.getImageCategory())) {
            replaceExisting(map.getDealerId(), image.getImageCategory(), image.getPlatform());
        }
        LocationImage savedImage = locationImageRepository.save(image);
        map.setImageId(savedImage.getImageId());
        locationImageMapRepository.save(map);
    }

    // For singleton categories (LOGO/COVER/PROFILE), Google reuses the same
    // media resource id across every replacement of the photo, so change detection
    // can't rely on consoleImageId — it's tracked via the separate versionMarker
    // column instead (see GMBMediaFetchTask). A null marker is unresolvable, so
    // treat it as changed rather than risk silently skipping a real update.
    @Transactional(readOnly = true)
    public boolean isCurrentSlotVersion(String dealerId, String category, String platform, String versionMarker) {
        if (versionMarker == null) {
            return false;
        }
        return locationImageMapRepository.findByDealerIdAndStatusAndImageCategoryAndPlatform(
                    dealerId, LocationImage.STATUS_DEPLOYED, category, platform)
                .map(LocationImageMap::getVersionMarker)
                .map(versionMarker::equals)
                .orElse(false);
    }

    // Gallery categories (everything except LOGO/COVER/PROFILE) allow many images
    // per dealer, so there's no single slot to replace — instead, once a fetch for
    // a dealer completes successfully, retire any previously-deployed row whose
    // consoleImageId is no longer among what GMB just reported at all, i.e. it was
    // deleted directly on the Google console. Deliberately NOT scoped by category:
    // Google's reported category for an item isn't guaranteed stable between
    // fetches (e.g. a missing location association falls back to ADDITIONAL), but
    // its resource id is durable and dealer-unique, so that's the only thing safe
    // to compare on — bucketing by category previously caused a still-existing
    // photo whose reported category drifted to be wrongly retired alongside the
    // one actually deleted. Singleton categories are excluded: they're already
    // handled by replaceExisting, and their consoleImageId is a real GMB resource
    // id rather than a fetched-item id.
    @Transactional
    public int retireDeletedGalleryImages(String dealerId, String platform, Set<String> fetchedResourceIds) {
        List<LocationImageMap> deployed = locationImageMapRepository.findAllByDealerIdAndStatusAndImagePlatform(
                dealerId, LocationImage.STATUS_DEPLOYED, platform);

        List<LocationImageMap> stale = deployed.stream()
                .filter(m -> !isSingletonCategory(m.getLocationImage().getImageCategory()))
                .filter(m -> !fetchedResourceIds.contains(m.getConsoleImageId()))
                .collect(Collectors.toList());

        deleteMapsAndOrphanedImages(stale);
        return stale.size();
    }

    // -----------------------------------------------------------------------
    // GET IMAGE DATA (PAGINATED)
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ImageDataPageResponse getImageData(ImageFilterRequest req) {
        Pageable pageable = PageRequest.of(req.getPageNo(), 10, Sort.by("imageId").descending());
        Page<LocationImage> imagePage = fetchFilteredImages(req, pageable);

        if (imagePage.isEmpty()) {
            return ImageDataPageResponse.builder()
                    .imageDataResponseList(List.of())
                    .totalNoOfPages(0)
                    .totalNoOfRecords(0L)
                    .build();
        }

        Set<Long> imageIds = imagePage.getContent().stream()
                .map(LocationImage::getImageId)
                .collect(Collectors.toSet());

        // Single query for all location maps on this page — avoids N+1 per image item.
        List<LocationImageMap> allMaps = locationImageMapRepository.findAllByImageIdIn(imageIds);

        Map<Long, List<LocationImageMap>> mapByImageId = allMaps.stream()
                .collect(Collectors.groupingBy(LocationImageMap::getImageId));

        List<ImageDataResponse> responseList = imagePage.getContent().stream()
                .map(image -> {
                    int dealerCount = mapByImageId.getOrDefault(image.getImageId(), List.of()).size();
                    return ImageDataResponse.builder()
                            .imageId(image.getImageId())
                            .imageUrl(image.getImageUrl())
                            .imageCategory(image.getImageCategory())
                            .imageFormat(image.getImageFormat())
                            .description(image.getDescription())
                            .label(image.getLabel())
                            .status(image.getStatus())
                            .createdDate(image.getCreatedDate())
                            .dealers(dealerCount)
                            .build();
                })
                .toList();

        return ImageDataPageResponse.builder()
                .imageDataResponseList(responseList)
                .totalNoOfPages(imagePage.getTotalPages())
                .totalNoOfRecords(imagePage.getTotalElements())
                .build();
    }

    // -----------------------------------------------------------------------
    // DEPLOYMENT TASK HELPERS
    // -----------------------------------------------------------------------

    public List<LocationImageMap> getAllLocationImageMapByStatus(String status) {
        return locationImageMapRepository.findAllByStatus(status);
    }

    public List<LocationImageMap> getAllLocationImageMapByStatusAndPlatform(String status, String platform) {
        return locationImageMapRepository.findAllByStatusAndImagePlatform(status, platform);
    }

    public void updateLocationImageMapStatus(String setStatus, String currentStatus) {
        locationImageMapRepository.updateAllByStatus(setStatus, currentStatus);
    }

    public void updateLocationImageMapStatusByPlatform(String setStatus, String currentStatus, String platform) {
        locationImageMapRepository.updateAllByStatusAndImagePlatform(setStatus, currentStatus, platform);
    }

    public void updateConsoleImageIdAndStatus(String consoleImageId, String status, String dealerId, Long imageId) {
        locationImageMapRepository.updateConsoleImageIdAndStatusByImageIdAndDealerId(
                consoleImageId, status, dealerId, imageId);
    }

    public LocationImage getImageByClientIdAndImageId(String clientId, Long imageId) {
        return locationImageRepository.findByClientIdAndImageId(clientId, imageId);
    }

    // Batch fetch used by the deployment task to avoid N+1 queries in the loop.
    public Map<Long, LocationImage> getImageMapByIds(Collection<Long> imageIds) {
        return locationImageRepository.findAllByImageIdIn(imageIds).stream()
                .collect(Collectors.toMap(LocationImage::getImageId, i -> i));
    }

    // -----------------------------------------------------------------------
    // INTERNAL HELPERS
    // -----------------------------------------------------------------------

    private Page<LocationImage> fetchFilteredImages(ImageFilterRequest req, Pageable pageable) {
        boolean hasCategory = req.getImageCategory() != null && !req.getImageCategory().isBlank();
        boolean hasStatus = req.getStatus() != null && !req.getStatus().isBlank();

        if (hasCategory && hasStatus) {
            return locationImageRepository.findAllByClientIdAndImageCategoryAndStatus(
                    req.getClientId(), req.getImageCategory(), req.getStatus(), pageable);
        } else if (hasCategory) {
            return locationImageRepository.findAllByClientIdAndImageCategory(
                    req.getClientId(), req.getImageCategory(), pageable);
        } else if (hasStatus) {
            return locationImageRepository.findAllByClientIdAndStatus(
                    req.getClientId(), req.getStatus(), pageable);
        } else {
            return locationImageRepository.findAllByClientId(req.getClientId(), pageable);
        }
    }

    private void validate(UploadImageRequest req, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new InvalidRequestException("at least one image file is required");
        }
        if (imageFiles.size() > MAX_IMAGES_PER_UPLOAD) {
            throw new InvalidRequestException("cannot upload more than " + MAX_IMAGES_PER_UPLOAD + " images at once");
        }
        if (imageFiles.stream().anyMatch(f -> f == null || f.isEmpty())) {
            throw new InvalidRequestException("image file is required");
        }
        if (imageFiles.size() > 1 && req.getImageCategory() != null
                && isSingletonCategory(normalizeCategory(req.getImageCategory()))) {
            throw new InvalidRequestException("only one image is allowed for LOGO/COVER/PROFILE");
        }
        if (req.getClientId() == null || req.getClientId().isBlank()) {
            throw new InvalidRequestException("client_id is required");
        }
        if (req.getDealerId() == null || req.getDealerId().isEmpty()) {
            throw new InvalidRequestException("dealer_id list is required and must not be empty");
        }
        if (req.getPlatform() == null || req.getPlatform().isBlank()) {
            throw new InvalidRequestException("platform is required");
        }
        if (!LocationImage.PLATFORM_GMB.equalsIgnoreCase(req.getPlatform())
                && !LocationImage.PLATFORM_FACEBOOK.equalsIgnoreCase(req.getPlatform())) {
            throw new InvalidRequestException("platform must be 'gmb' or 'facebook'");
        }
        if (req.getImageCategory() == null || req.getImageCategory().isBlank()) {
            throw new InvalidRequestException("image_category is required");
        }
        if (req.getImageFormat() == null || req.getImageFormat().isBlank()) {
            throw new InvalidRequestException("image_format is required");
        }
        if (LocationImage.PLATFORM_FACEBOOK.equalsIgnoreCase(req.getPlatform())) {
            if (!LocationImage.IMAGE_FORMAT_PHOTO.equalsIgnoreCase(req.getImageFormat())) {
                throw new InvalidRequestException("image_format must be PHOTO for Facebook platform");
            }
        } else {
            if (!LocationImage.IMAGE_FORMAT_PHOTO.equalsIgnoreCase(req.getImageFormat())
                    && !LocationImage.IMAGE_FORMAT_VIDEO.equalsIgnoreCase(req.getImageFormat())) {
                throw new InvalidRequestException("image_format must be PHOTO or VIDEO");
            }
        }
    }
}
