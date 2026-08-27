package com.caliper.task;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.images.api.GmbImageAPI;
import com.caliper.images.entity.LocationImage;
import com.caliper.images.entity.LocationImageMap;
import com.caliper.images.service.LocationImageService;
import com.caliper.location.entity.Client;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.repository.GMBLocationRepository;
import com.caliper.location.gmb.service.GMBSessionFactory;
import com.caliper.location.repository.ClientRepository;
import com.caliper.location.service.ClientService;
import com.google.api.services.mybusiness.v4.MyBusiness;
import com.google.api.services.mybusiness.v4.model.ListMediaItemsResponse;
import com.google.api.services.mybusiness.v4.model.MediaItem;

@Service
public class GMBMediaFetchTask extends ParameterizedJob {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private GMBLocationRepository gmbLocationRepository;

    @Autowired
    private GMBSessionFactory gmbSessionFactory;

    @Autowired
    private GmbImageAPI gmbImageAPI;
    
	@Autowired
	public ClientService clientService;

    @Autowired
    private LocationImageService locationImageService;

    private List<Client> clientList;
    private boolean fetchGmbMedia;
    private String fetchDealerIds;
    private int mediaPageSize;
    private MyBusiness business;

    @Override
    public void run() {
        try {
            log("job started");
            init();
            fetchMedia();
            log("job end");
        } catch (Exception e) {
            log(e.getMessage());
        }
    }

    private void init() throws Exception {
        this.fetchGmbMedia = parameters.getBoolean("fetch-gmb-media", false);
        this.fetchDealerIds = parameters.getString("fetch-location-ids");
        this.mediaPageSize = parameters.getInt("media-page-size", 100);
    	String clientId = parameters.getString("client-id", null);
		if (clientId != null && !clientId.isBlank()) {
			this.clientList = List.of(clientService.findByClientId(clientId));
		} else {
			this.clientList = clientRepository.findAll();
		}
        log("no of clients : " + clientList.size());
    }

    private void fetchMedia() throws InterruptedException {
        if (!fetchGmbMedia) {
            return;
        }

        for (Client client : clientList) {
            try {
                this.business = gmbSessionFactory.getGMBSession(client.getClientId());
            } catch (Exception e) {
                log("Failed to get GMB session for client: " + client.getClientId() + " - " + e.getMessage());
                continue;
            }

            List<GMBLocation> gmbLocations = resolveLocations(client);
            log("client " + client.getClientId() + " : " + gmbLocations.size() + " locations to scan");

            for (GMBLocation loc : gmbLocations) {
                if (loc.getStatus() == null || !loc.getStatus().equalsIgnoreCase(GMBLocation.LOCATION_STATE_VERIFIED)) {
                    log("Skipping dealer " + loc.getDealerId() + " - status is not verified");
                    continue;
                }
                try {
                    fetchMediaForLocation(client, loc);
                } catch (Exception ex) {
                    log("Error fetching media for dealer " + loc.getDealerId() + " : " + ex.getMessage());
                }
                Thread.sleep(500);
            }
        }
    }

    private List<GMBLocation> resolveLocations(Client client) {
        if (fetchDealerIds != null && !fetchDealerIds.isBlank()) {
            List<String> dealerIds = Arrays.stream(fetchDealerIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            return gmbLocationRepository.findByClientIdAndDealerIdIn(client.getClientId(), dealerIds);
        }
        return gmbLocationRepository.findByClientId(client.getClientId());
    }

    private void fetchMediaForLocation(Client client, GMBLocation loc) throws Exception {
        String pageToken = "";
        boolean getNextPage = true;
        Set<String> fetchedResourceIds = new HashSet<>();

        while (getNextPage) {
            ListMediaItemsResponse response = null;
            for (int i = 0; i < 4; i++) {
                try {
                    response = gmbImageAPI.listMediaForLocation(business, loc.getGmbLocationId(), pageToken, mediaPageSize);
                    break;
                } catch (Exception ex) {
                    if (i >= 3) {
                        throw ex;
                    }
                    log("Error listing media for " + loc.getDealerId() + ", retrying in 30s: " + ex.getMessage());
                    Thread.sleep(30000);
                }
            }

            if (response == null || response.getMediaItems() == null) {
                break;
            }

            log("Fetched " + response.getMediaItems().size() + " media items for dealer " + loc.getDealerId());

            for (MediaItem item : response.getMediaItems()) {
                try {
                    saveMediaItem(client, loc, item, fetchedResourceIds);
                } catch (Exception ex) {
                    log("Error saving media item " + item.getName() + " for dealer " + loc.getDealerId()
                            + " : " + ex.getMessage());
                }
            }

            pageToken = response.getNextPageToken();
            getNextPage = pageToken != null && !pageToken.isBlank();
        }

        // Only reached once every page has been fetched successfully, so
        // fetchedResourceIds reflects everything currently on Google for this
        // location — safe to retire any gallery-category row no longer in it
        // (e.g. deleted directly on the Google console).
        int retiredCount = locationImageService.retireDeletedGalleryImages(
                loc.getDealerId(), LocationImage.PLATFORM_GMB, fetchedResourceIds);
        if (retiredCount > 0) {
            log("Retired " + retiredCount + " deleted gallery photo(s) for dealer " + loc.getDealerId());
        }
    }

    private void saveMediaItem(Client client, GMBLocation loc, MediaItem item, Set<String> fetchedResourceIds) {
        String resourceId = item.getName().substring(item.getName().lastIndexOf('/') + 1);

        String category = (item.getLocationAssociation() != null && item.getLocationAssociation().getCategory() != null)
                ? item.getLocationAssociation().getCategory()
                : LocationImage.CATEGORY_ADDITIONAL;

        // Recorded before anything below can throw, so an item is counted as
        // "currently on Google" even if persisting it locally later fails. Not
        // bucketed by category: Google's reported category for an item isn't
        // guaranteed stable between fetches, but its resource id is durable and
        // dealer-unique, so it's the only thing safe to key deletion-detection on.
        fetchedResourceIds.add(resourceId);

        String imageUrl = item.getGoogleUrl() != null ? item.getGoogleUrl() : item.getThumbnailUrl();

        boolean singleton = locationImageService.isSingletonCategory(category);
        String consoleImageId = resourceId;
        String versionMarker = null;
        boolean alreadyCurrent;
        if (singleton) {
            // Google reuses the same resource id ("...media/profile") across every
            // replacement of a logo/cover/profile photo, so the id can't be used to
            // detect a change. createTime isn't reliable either — GMB returns a
            // constant placeholder value for this category rather than omitting it,
            // so it never changes between fetches. Track changes via a separate
            // version_marker column holding the URL minus any query string instead,
            // since it's content-addressed and reliably changes whenever the photo
            // itself changes, while console_image_id keeps holding the real resource id.
            versionMarker = stripQuery(imageUrl);
            alreadyCurrent = locationImageService.isCurrentSlotVersion(
                    loc.getDealerId(), category, LocationImage.PLATFORM_GMB, versionMarker);
        } else {
            alreadyCurrent = locationImageService.alreadyImported(loc.getDealerId(), resourceId);
        }
        if (alreadyCurrent) {
            return;
        }

        LocationImage image = LocationImage.builder()
                .clientId(client.getClientId())
                .imageUrl(imageUrl)
                .imageCategory(category)
                .imageFormat(item.getMediaFormat())
                .description(item.getDescription())
                .createdBy("GMB_SYNC")
                .createdDate(new Date())
                .status(LocationImage.STATUS_DEPLOYED)
                .platform(LocationImage.PLATFORM_GMB)
                .build();

        LocationImageMap map = LocationImageMap.builder()
                .dealerId(loc.getDealerId())
                .clientId(client.getClientId())
                .status(LocationImage.STATUS_DEPLOYED)
                .createdDate(new Date())
                .consoleImageId(consoleImageId)
                .versionMarker(versionMarker)
                .build();

        // persistFetchedGmbMedia retires any prior logo/cover/profile for this dealer
        // before recording the one GMB now reports as current (handles the case where
        // it was changed directly on the Google Business Profile, not via Caliper's UI).
        locationImageService.persistFetchedGmbMedia(image, map);
        log("Imported media " + resourceId + " (" + category + ") for dealer " + loc.getDealerId());
    }

    private static String stripQuery(String url) {
        if (url == null) {
            return null;
        }
        int q = url.indexOf('?');
        return q >= 0 ? url.substring(0, q) : url;
    }
}
