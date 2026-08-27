package com.caliper.task;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.campaign.facebook.entity.MetaAd;
import com.caliper.campaign.facebook.entity.MetaAdCarouselCard;
import com.caliper.campaign.facebook.entity.MetaAdCreative;
import com.caliper.campaign.facebook.entity.MetaAdImage;
import com.caliper.campaign.facebook.entity.MetaAdImageAsset;
import com.caliper.campaign.facebook.entity.MetaAdSet;
import com.caliper.campaign.facebook.entity.MetaCampaign;
import com.caliper.campaign.facebook.repository.MetaAdCarouselCardRepository;
import com.caliper.campaign.facebook.repository.MetaAdCreativeRepository;
import com.caliper.campaign.facebook.repository.MetaAdImageAssetRepository;
import com.caliper.campaign.facebook.repository.MetaAdImageRepository;
import com.caliper.campaign.facebook.repository.MetaAdRepository;
import com.caliper.campaign.facebook.repository.MetaAdSetRepository;
import com.caliper.campaign.facebook.repository.MetaCampaignRepository;
import com.caliper.campaign.facebook.service.MetaCampaignService;
import com.caliper.location.facebook.entity.FacebookAccount;
import com.caliper.location.facebook.repository.FacebookAccountRepository;
import com.caliper.metaads.service.MetaAdsApiService;
import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.APIException;
import com.facebook.ads.sdk.AdPromotedObject;
import com.facebook.ads.sdk.Targeting;
import com.google.gson.JsonObject;

@Service
public class MetaAdsCampaignDeploymentTask extends ParameterizedJob {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_BASE_DELAY_MS = 1000L;
    private static final int RATE_LIMIT_ERROR_CODE = 17;
    private static final long RATE_LIMIT_WAIT_MS = 60_000L;

    @Autowired
    private MetaCampaignService metaCampaignService;

    @Autowired
    private MetaCampaignRepository metaCampaignRepository;

    @Autowired
    private MetaAdSetRepository metaAdSetRepository;

    @Autowired
    private MetaAdCreativeRepository metaAdCreativeRepository;

    @Autowired
    private MetaAdRepository metaAdRepository;

    @Autowired
    private MetaAdImageRepository metaAdImageRepository;

    @Autowired
    private MetaAdImageAssetRepository metaAdImageAssetRepository;

    @Autowired
    private MetaAdCarouselCardRepository metaAdCarouselCardRepository;

    @Autowired
    private FacebookAccountRepository facebookAccountRepository;

    @Override
    public void run() {
        try {
            init();
            deployMetaCampaigns();
        } catch (Exception e) {
            log("Fatal error in MetaAdsCampaignDeploymentTask: " + e.getMessage());
        }
    }

    private void deployMetaCampaigns() {
        List<MetaCampaign> campaigns = metaCampaignService.findAllByStatus(MetaCampaign.STATUS_PENDING_DEPLOYMENT);
        log("Found " + campaigns.size() + " Meta campaign(s) pending deployment");

        for (MetaCampaign campaign : campaigns) {
            try {
                deploySingleCampaign(campaign);
            } catch (Exception e) {
                log("Error deploying campaign id=" + campaign.getId() + ": " + e.getMessage());
                markCampaignError(campaign, e.getMessage());
            }
        }
    }

    private void deploySingleCampaign(MetaCampaign campaign) throws Exception {
        long campaignId = campaign.getId();
        String clientId = campaign.getClientId();

        log("Processing Meta campaign: " + campaign.getCampaignName() + " (id=" + campaignId + ")");

        String dealerId = campaign.getDealerId();
        FacebookAccount account = facebookAccountRepository
                .findByClientIdAndDealerId(clientId, dealerId)
                .or(() -> facebookAccountRepository.findByClientIdAndDealerIdIsNull(clientId))
                .orElseThrow(() -> new IllegalStateException(
                        "FacebookAccount not found for clientId=" + clientId + " dealerId=" + dealerId));

        APIContext ctx = MetaAdsApiService.getApiContext(account.getAccessToken());
        String adAccountId = "act_" + account.getAccountId();

        // Fetch adSet early — needed for campaign bid strategy
        MetaAdSet adSet = metaAdSetRepository.findFirstByCampaignId(campaignId)
                .orElseThrow(() -> new IllegalStateException("AdSet not found for campaign: " + campaignId));

        // ── STEP 1: CREATE CAMPAIGN ──────────────────────────────────────────
        if ("-1".equals(campaign.getMetaCampaignId())) {
            log("Creating Meta campaign: " + campaign.getCampaignName());
            String budgetType = campaign.getBudgetType();
            double budgetAmount = "LIFETIME".equalsIgnoreCase(budgetType)
                    ? campaign.getTotalBudget().doubleValue()
                    : campaign.getDailyBudget().doubleValue();
            String metaCampaignId = executeWithRetry(() ->
                    MetaAdsApiService.createCampaign(
                            ctx, adAccountId,
                            campaign.getCampaignName(),
                            campaign.getObjective(),
                            budgetType,
                            budgetAmount,
                            toIsoString(campaign.getStartTime()),
                            toIsoString(campaign.getStopTime()),
                            campaign.getDestinationType(),
                            adSet.getBidStrategy()),
                    "createCampaign");
            campaign.setMetaCampaignId(metaCampaignId);
            metaCampaignRepository.save(campaign);
            log("Created Meta campaign id: " + metaCampaignId);
        }

        // ── STEP 2: CREATE AD SET ────────────────────────────────────────────

        if ("-1".equals(adSet.getMetaAdSetId())) {
            log("Creating Meta AdSet: " + adSet.getAdSetName());
            Targeting targeting = buildTargeting(adSet);
            AdPromotedObject promotedObject = MetaAdsApiService.buildPromotedObject(
                    campaign.getObjective(),
                    adSet.getOptimizationGoal(),
                    campaign.getDestinationType(),
                    adSet.getPromotedObjectPageId(),
                    adSet.getPromotedObjectPixelId(),
                    adSet.getPromotedObjectCustomEventType(),
                    adSet.getWhatsappNumber());
            String metaAdSetId = executeWithRetry(() ->
                    MetaAdsApiService.createAdSet(
                            ctx, adAccountId,
                            campaign.getMetaCampaignId(),
                            adSet.getAdSetName(),
                            targeting,
                            adSet.getOptimizationGoal(),
                            adSet.getBillingEvent(),
                            adSet.getBidStrategy(),
                            adSet.getBidAmount(),
                            null,
                            toIsoString(adSet.getStartTime()),
                            toIsoString(adSet.getStopTime()),
                            promotedObject),
                    "createAdSet");
            adSet.setMetaAdSetId(metaAdSetId);
            metaAdSetRepository.save(adSet);
            log("Created Meta AdSet id: " + metaAdSetId);
        }

        // ── STEP 3 + 4: HANDLE CREATIVE BY TYPE ─────────────────────────────
        MetaAdCreative creative = metaAdCreativeRepository.findFirstByCampaignId(campaignId)
                .orElseThrow(() -> new IllegalStateException("AdCreative not found for campaign: " + campaignId));

        String creativeType = creative.getCreativeType() != null
                ? creative.getCreativeType() : MetaAdCreative.CREATIVE_TYPE_IMAGE;

        if (MetaAdCreative.CREATIVE_TYPE_IMAGE.equals(creativeType)) {
            deployImageCreative(ctx, adAccountId, campaignId, creative);
        } else if (MetaAdCreative.CREATIVE_TYPE_VIDEO.equals(creativeType)) {
            deployVideoCreative(ctx, adAccountId, creative);
        } else if (MetaAdCreative.CREATIVE_TYPE_CAROUSEL.equals(creativeType)) {
            deployCarouselCreative(ctx, adAccountId, campaignId, creative);
        }

        // ── STEP 5: CREATE AD ────────────────────────────────────────────────
        // Reload creative to get latest metaCreativeId
        MetaAdCreative updatedCreative = metaAdCreativeRepository.findFirstByCampaignId(campaignId)
                .orElseThrow(() -> new IllegalStateException("AdCreative not found for campaign: " + campaignId));

        List<MetaAd> ads = metaAdRepository.findByCampaignId(campaignId);
        if (!ads.isEmpty()) {
            MetaAd ad = ads.get(0);
            if ("-1".equals(ad.getMetaAdId())) {
                log("Creating Meta Ad: " + ad.getAdName());
                String metaAdId = executeWithRetry(() ->
                        MetaAdsApiService.createAd(
                                ctx, adAccountId,
                                adSet.getMetaAdSetId(),
                                updatedCreative.getMetaCreativeId(),
                                ad.getAdName()),
                        "createAd");
                ad.setMetaAdId(metaAdId);
                metaAdRepository.save(ad);
                log("Created Meta Ad id: " + metaAdId);
            }
        }

        // ── STEP 6: MARK DEPLOYED ────────────────────────────────────────────
        campaign.setStatus(MetaCampaign.STATUS_DEPLOYED);
        campaign.setLastModifiedDate(new Date());
        metaCampaignRepository.save(campaign);
        log("Campaign " + campaign.getCampaignName() + " deployed successfully");
    }

    private void deployImageCreative(APIContext ctx, String adAccountId,
            long campaignId, MetaAdCreative creative) throws Exception {

        // Check if multi-image assets are stored for this campaign
        List<MetaAdImageAsset> imageAssets = metaAdImageAssetRepository
                .findByCampaignIdOrderByAssetOrderAsc(campaignId);

        if (!imageAssets.isEmpty()) {
            // ── Multi-image (asset_feed_spec) path ───────────────────────────
            for (MetaAdImageAsset asset : imageAssets) {
                if ("-1".equals(asset.getImageHash())
                        && asset.getImageUrl() != null && !asset.getImageUrl().isBlank()) {
                    log("Uploading image asset [" + asset.getRatio() + "]: " + asset.getImageUrl());
                    String hash = executeWithRetry(() ->
                            MetaAdsApiService.uploadImageFromUrl(ctx, adAccountId, asset.getImageUrl()),
                            "uploadImageAsset");
                    asset.setImageHash(hash);
                    metaAdImageAssetRepository.save(asset);
                    log("Image asset uploaded, hash: " + hash);
                }
            }

            if ("-1".equals(creative.getMetaCreativeId())) {
                List<String> hashes = imageAssets.stream()
                        .map(MetaAdImageAsset::getImageHash)
                        .filter(h -> h != null && !"-1".equals(h))
                        .collect(java.util.stream.Collectors.toList());
                log("Creating multi-image AdCreative (" + hashes.size() + " images): " + creative.getName());
                String metaCreativeId = executeWithRetry(() ->
                        MetaAdsApiService.createAssetFeedAdCreative(
                                ctx, adAccountId,
                                creative.getPageId(), creative.getHeadline(), creative.getBody(),
                                creative.getDescription(), hashes,
                                creative.getLinkUrl(), creative.getCallToActionType(),
                                creative.isDegreesOfFreedom()),
                        "createAssetFeedAdCreative");
                creative.setMetaCreativeId(metaCreativeId);
                metaAdCreativeRepository.save(creative);
                log("Created multi-image AdCreative id: " + metaCreativeId);
            }
            return;
        }

        // ── Single image path ────────────────────────────────────────────────
        List<MetaAdImage> images = metaAdImageRepository.findByCampaignId(campaignId);
        MetaAdImage adImage = images.isEmpty() ? null : images.get(0);

        if ("-1".equals(creative.getImageHash()) && creative.getImageUrl() != null && !creative.getImageUrl().isBlank()) {
            log("Uploading image: " + creative.getImageUrl());
            String imageHash = executeWithRetry(() ->
                    MetaAdsApiService.uploadImageFromUrl(ctx, adAccountId, creative.getImageUrl()),
                    "uploadImage");
            creative.setImageHash(imageHash);
            metaAdCreativeRepository.save(creative);
            if (adImage != null) {
                adImage.setImageHash(imageHash);
                adImage.setStatus(MetaAdImage.STATUS_UPLOADED);
                metaAdImageRepository.save(adImage);
            }
            log("Image uploaded, hash: " + imageHash);
        }

        if ("-1".equals(creative.getMetaCreativeId())) {
            log("Creating image AdCreative: " + creative.getName());
            String metaCreativeId = executeWithRetry(() ->
                    MetaAdsApiService.createAdCreative(
                            ctx, adAccountId,
                            creative.getPageId(), creative.getHeadline(), creative.getBody(),
                            creative.getDescription(), creative.getImageHash(),
                            creative.getLinkUrl(), creative.getCallToActionType(),
                            creative.isDegreesOfFreedom()),
                    "createAdCreative");
            creative.setMetaCreativeId(metaCreativeId);
            metaAdCreativeRepository.save(creative);
            log("Created image AdCreative id: " + metaCreativeId);
        }
    }

    private void deployVideoCreative(APIContext ctx, String adAccountId,
            MetaAdCreative creative) throws Exception {

        if ("-1".equals(creative.getVideoId()) && creative.getVideoUrl() != null && !creative.getVideoUrl().isBlank()) {
            log("Uploading video: " + creative.getVideoUrl());
            String videoId = executeWithRetry(() ->
                    MetaAdsApiService.uploadVideoFromUrl(ctx, adAccountId, creative.getVideoUrl()),
                    "uploadVideo");
            creative.setVideoId(videoId);
            metaAdCreativeRepository.save(creative);
            log("Video uploaded, id: " + videoId);
        }

        // Wait for Meta to finish processing the uploaded video before using it in a creative
        if (!"-1".equals(creative.getVideoId())) {
            log("Waiting for video id=" + creative.getVideoId() + " to finish processing...");
            MetaAdsApiService.waitForVideoReady(ctx, creative.getVideoId(), 600);
            log("Video is ready");
        }

        if ("-1".equals(creative.getThumbnailHash()) && creative.getThumbnailUrl() != null && !creative.getThumbnailUrl().isBlank()) {
            log("Uploading video thumbnail: " + creative.getThumbnailUrl());
            String thumbnailHash = executeWithRetry(() ->
                    MetaAdsApiService.uploadImageFromUrl(ctx, adAccountId, creative.getThumbnailUrl()),
                    "uploadThumbnail");
            creative.setThumbnailHash(thumbnailHash);
            metaAdCreativeRepository.save(creative);
            log("Thumbnail uploaded, hash: " + thumbnailHash);
        }

        if ("-1".equals(creative.getMetaCreativeId())) {
            log("Creating video AdCreative: " + creative.getName());
            String metaCreativeId = executeWithRetry(() ->
                    MetaAdsApiService.createVideoAdCreative(
                            ctx, adAccountId,
                            creative.getPageId(), creative.getHeadline(), creative.getBody(),
                            creative.getDescription(), creative.getVideoId(),
                            creative.getLinkUrl(), creative.getCallToActionType(),
                            creative.getThumbnailHash(),
                            creative.isDegreesOfFreedom()),
                    "createVideoAdCreative");
            creative.setMetaCreativeId(metaCreativeId);
            metaAdCreativeRepository.save(creative);
            log("Created video AdCreative id: " + metaCreativeId);
        }
    }

    private void deployCarouselCreative(APIContext ctx, String adAccountId,
            long campaignId, MetaAdCreative creative) throws Exception {

        List<MetaAdCarouselCard> cards = metaAdCarouselCardRepository
                .findByCampaignIdOrderByCardOrderAsc(campaignId);
        if (cards.isEmpty()) {
            throw new IllegalStateException("No carousel cards found for campaign: " + campaignId);
        }

        // Upload images for any card missing a hash
        for (MetaAdCarouselCard card : cards) {
            if ("-1".equals(card.getImageHash()) && card.getImageUrl() != null && !card.getImageUrl().isBlank()) {
                log("Uploading carousel card image: " + card.getImageUrl());
                String hash = executeWithRetry(() ->
                        MetaAdsApiService.uploadImageFromUrl(ctx, adAccountId, card.getImageUrl()),
                        "uploadCarouselImage");
                card.setImageHash(hash);
                metaAdCarouselCardRepository.save(card);
                log("Carousel card[" + card.getCardOrder() + "] image hash: " + hash);
            }
        }

        if ("-1".equals(creative.getMetaCreativeId())) {
            log("Creating carousel AdCreative: " + creative.getName());
            List<java.util.Map<String, String>> cardData = cards.stream()
                    .map(c -> {
                        java.util.Map<String, String> m = new java.util.LinkedHashMap<>();
                        m.put("imageHash",   c.getImageHash());
                        m.put("linkUrl",     c.getLinkUrl());
                        m.put("headline",    c.getHeadline());
                        m.put("description", c.getDescription());
                        return m;
                    })
                    .collect(java.util.stream.Collectors.toList());

            String metaCreativeId = executeWithRetry(() ->
                    MetaAdsApiService.createCarouselAdCreative(
                            ctx, adAccountId,
                            creative.getPageId(), creative.getBody(),
                            creative.getLinkUrl(), creative.getCallToActionType(),
                            cardData, creative.isDegreesOfFreedom()),
                    "createCarouselAdCreative");
            creative.setMetaCreativeId(metaCreativeId);
            metaAdCreativeRepository.save(creative);
            log("Created carousel AdCreative id: " + metaCreativeId);
        }
    }

    // ──────────────── Retry Mechanism ────────────────────────────────────────

    @FunctionalInterface
    private interface ApiCall<T> {
        T execute() throws APIException;
    }

    private <T> T executeWithRetry(ApiCall<T> call, String operationName) throws Exception {
        int attempt = 0;
        while (true) {
            try {
                return call.execute();
            } catch (APIException e) {
                attempt++;
                int errorCode = extractErrorCode(e);
                log("API error [" + operationName + "] code=" + errorCode + " attempt=" + attempt + ": " + e.getMessage());

                if (errorCode == RATE_LIMIT_ERROR_CODE) {
                    if (attempt >= MAX_RETRIES) throw e;
                    log("Rate limit hit, waiting " + RATE_LIMIT_WAIT_MS / 1000 + "s before retry...");
                    Thread.sleep(RATE_LIMIT_WAIT_MS);
                } else if (errorCode == 2 || errorCode == 1 || errorCode == 368) {
                    // Transient errors: exponential backoff
                    if (attempt >= MAX_RETRIES) throw e;
                    long delay = RETRY_BASE_DELAY_MS * (1L << (attempt - 1));
                    log("Transient error, retrying in " + delay + "ms...");
                    Thread.sleep(delay);
                } else if (extractErrorSubcode(e) == 1885252) {
                    // Video still processing — wait 30s and retry
                    if (attempt >= MAX_RETRIES) throw e;
                    log("Video not ready yet (subcode 1885252), waiting 30s before retry...");
                    Thread.sleep(30_000L);
                } else {
                    throw e;
                }
            }
        }
    }

    // ──────────────── Helpers ────────────────────────────────────────────────

    private int extractErrorCode(APIException e) {
        try {
            JsonObject json = e.getRawResponseAsJsonObject();
            if (json != null && json.has("error")) {
                JsonObject error = json.getAsJsonObject("error");
                if (error != null && error.has("code")) {
                    return error.get("code").getAsInt();
                }
            }
        } catch (Exception ignored) {
            log("Could not parse Facebook error code: " + ignored.getMessage());
        }
        return -1;
    }

    private int extractErrorSubcode(APIException e) {
        try {
            JsonObject json = e.getRawResponseAsJsonObject();
            if (json != null && json.has("error")) {
                JsonObject error = json.getAsJsonObject("error");
                if (error != null && error.has("error_subcode")) {
                    return error.get("error_subcode").getAsInt();
                }
            }
        } catch (Exception ignored) {
            log("Could not parse Facebook error subcode: " + ignored.getMessage());
        }
        return -1;
    }

    private Targeting buildTargeting(MetaAdSet adSet) {
        String type = adSet.getTargetingType();
        Targeting targeting;
        if (MetaAdSet.TARGETING_TYPE_CITY.equals(type)) {
            targeting = MetaAdsApiService.buildCityTargeting(adSet.getCity());
        } else if (MetaAdSet.TARGETING_TYPE_REGION.equals(type)) {
            targeting = MetaAdsApiService.buildRegionTargeting(adSet.getRegion());
        } else if (MetaAdSet.TARGETING_TYPE_PINCODE.equals(type)) {
            targeting = MetaAdsApiService.buildZipTargeting(adSet.getPincode());
        } else {
            double lat = Double.parseDouble(adSet.getLatitude());
            double lng = Double.parseDouble(adSet.getLongitude());
            targeting = MetaAdsApiService.buildRadiusTargeting(lat, lng, adSet.getRadius(), adSet.getRadiusUnit());
        }

        MetaAdsApiService.applyAudienceTargeting(
                targeting,
                adSet.getGender(),
                adSet.getAgeMin(),
                adSet.getAgeMax(),
                splitComma(adSet.getPublisherPlatforms()),
                splitComma(adSet.getFacebookPositions()),
                splitComma(adSet.getInstagramPositions()));

        return targeting;
    }

    private java.util.List<String> splitComma(String csv) {
        if (csv == null || csv.isBlank()) return new java.util.ArrayList<>();
        return java.util.Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    private void markCampaignError(MetaCampaign campaign, String errorMessage) {
        try {
            campaign.setStatus(MetaCampaign.STATUS_ERROR);
            campaign.setErrorComment(errorMessage != null
                    ? errorMessage.substring(0, Math.min(errorMessage.length(), 500))
                    : "Unknown error");
            campaign.setLastModifiedDate(new Date());
            metaCampaignRepository.save(campaign);
        } catch (Exception e) {
            log("Failed to mark campaign as ERROR: " + e.getMessage());
        }
    }

    private String toIsoString(Date date) {
        if (date == null) return null;
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(date);
    }

    private void init() {
        // parameters available via ParameterizedJob; no per-client ID needed —
        // the task processes all PENDING_DEPLOYMENT campaigns
    }
}
