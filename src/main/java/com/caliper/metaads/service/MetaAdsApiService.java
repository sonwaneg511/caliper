package com.caliper.metaads.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.caliper.utils.exception.customException.InvalidRequestException;
import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.APIException;
import com.facebook.ads.sdk.APINodeList;
import com.facebook.ads.sdk.Ad;
import com.facebook.ads.sdk.AdAccount;
import com.facebook.ads.sdk.AdCreative;
import com.facebook.ads.sdk.AdCreativeLinkData;
import com.facebook.ads.sdk.AdCreativeLinkDataCallToAction;
import com.facebook.ads.sdk.AdCreativeLinkDataCallToActionValue;
import com.facebook.ads.sdk.AdCreativeLinkDataChildAttachment;
import com.facebook.ads.sdk.AdCreativeObjectStorySpec;
import com.facebook.ads.sdk.AdCreativeVideoData;
import com.facebook.ads.sdk.AdImage;
import com.facebook.ads.sdk.AdPromotedObject;
import com.facebook.ads.sdk.AdSet;
import com.facebook.ads.sdk.AdVideo;
import com.facebook.ads.sdk.AdsInsights;
import com.facebook.ads.sdk.Campaign;
import com.facebook.ads.sdk.Campaign.EnumBidStrategy;
import com.facebook.ads.sdk.Campaign.EnumSpecialAdCategories;
import com.facebook.ads.sdk.Lead;
import com.facebook.ads.sdk.Targeting;
import com.facebook.ads.sdk.TargetingAutomation;
import com.facebook.ads.sdk.TargetingGeoLocation;
import com.facebook.ads.sdk.TargetingGeoLocationCity;
import com.facebook.ads.sdk.TargetingGeoLocationCustomLocation;
import com.facebook.ads.sdk.TargetingGeoLocationRegion;
import com.facebook.ads.sdk.TargetingGeoLocationZip;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MetaAdsApiService {

    private MetaAdsApiService() {}

    public static APIContext getApiContext(String accessToken) {
        return new APIContext(accessToken).enableDebug(false);
    }

    /**
     * Creates a Meta campaign in PAUSED state.
     *
     * @param budgetType       "DAILY" or "LIFETIME" — controls which budget field is set
     * @param budgetAmount     Budget in the account's currency (e.g. 500 for ₹500). Converted to micro-units internally.
     * @param bidStrategy      Optional campaign-level bid strategy (LOWEST_COST_WITHOUT_CAP etc.)
     * @param destinationType  Optional (e.g. WEBSITE, APP, MESSENGER)
     */
    public static String createCampaign(APIContext ctx, String adAccountId, String name,
            String objective, String budgetType, double budgetAmount,
            String startTime, String stopTime,
            String destinationType, String bidStrategy) throws APIException {

        long budgetMicro = convertToFacebookAmount(budgetAmount);

        AdAccount.APIRequestCreateCampaign req = new AdAccount(adAccountId, ctx).createCampaign()
                .setName(name)
                .setObjective(mapObjective(objective))
                .setStatus(Campaign.EnumStatus.VALUE_PAUSED)
//                .setStartTime(startTime)
//                .setStopTime(stopTime)
                .setSpecialAdCategories(new ArrayList<EnumSpecialAdCategories>(Arrays.asList(EnumSpecialAdCategories.VALUE_NONE)));

        if ("LIFETIME".equalsIgnoreCase(budgetType)) {
            req.setLifetimeBudget(budgetMicro);
        } else {
            req.setDailyBudget(budgetMicro);
        }

        if (bidStrategy != null && !bidStrategy.isBlank()) {
            req.setBidStrategy(Campaign.EnumBidStrategy.valueOf("VALUE_" + bidStrategy));
        }

        if (destinationType != null && !destinationType.isBlank()) {
            req.setParam("destination_type", destinationType);
        }

        Campaign campaign = req.execute();
        return campaign.getId();
    }

    public static long convertToFacebookAmount(double amount) {
		long fbAmount = (long) (amount * 100);
		return fbAmount;
	}

    /**
     * Creates an AdSet.
     *
     * @param targeting        Pre-built geo targeting (from buildXxxTargeting helpers)
     * @param bidStrategy      Optional — LOWEST_COST_WITHOUT_CAP | LOWEST_COST_WITH_BID_CAP | COST_CAP | LOWEST_COST_WITH_MIN_ROAS
     * @param bidAmount        Optional bid cap in cents (used with LOWEST_COST_WITH_BID_CAP / COST_CAP)
     * @param dailyBudgetCents Pass null to use campaign-level budget
     * @param promotedObject   Optional — page, pixel, app promotion details
     */
    public static String createAdSet(APIContext ctx, String adAccountId, String campaignId,
            String adSetName, Targeting targeting, String optimizationGoal, String billingEvent,
            String bidStrategy, Long bidAmount, Long dailyBudgetCents,
            String startTime, String stopTime, AdPromotedObject promotedObject) throws APIException {

        AdAccount.APIRequestCreateAdSet request = new AdAccount(adAccountId, ctx).createAdSet()
                .setName(adSetName)
                .setCampaignId(campaignId)
                .setOptimizationGoal(AdSet.EnumOptimizationGoal.valueOf("VALUE_" + optimizationGoal))
                .setBillingEvent(AdSet.EnumBillingEvent.VALUE_IMPRESSIONS)
                .setStartTime(startTime)
                .setEndTime(stopTime)
                .setTargeting(targeting)
                .setStatus(AdSet.EnumStatus.VALUE_PAUSED);

        if (bidStrategy != null && !bidStrategy.isBlank()) {
            request.setBidStrategy(AdSet.EnumBidStrategy.valueOf("VALUE_" + bidStrategy));
        }

        if (bidAmount != null && bidAmount > 0) {
            request.setBidAmount(bidAmount);
        }

        if (dailyBudgetCents != null && dailyBudgetCents > 0) {
            request.setDailyBudget(dailyBudgetCents);
        }

        if (promotedObject != null) {
            request.setPromotedObject(promotedObject);
        }

        AdSet adSet = request.execute();
        return adSet.getId();
    }

    /**
     * Uploads an image to the Meta Ad Account by downloading it from a public URL first,
     * then uploading the file bytes. This avoids the capability restriction on URL-based uploads.
     */
    public static String uploadImageFromUrl(APIContext ctx, String adAccountId, String imageUrl) throws APIException {
        File tempFile = null;
        try {
            String ext = imageUrl.contains(".png") ? ".png" : ".jpg";
            tempFile = Files.createTempFile("caliper_image_", ext).toFile();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .GET()
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                throw new APIException("Failed to download image from URL (HTTP " + response.statusCode() + "): " + imageUrl);
            }
            try (InputStream in = response.body(); FileOutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }

            AdImage image = new AdAccount(adAccountId, ctx).createAdImage()
                    .addUploadFile("filename", tempFile)
                    .execute();

            if (image.getFieldHash() != null && !image.getFieldHash().isEmpty()) {
                return image.getFieldHash();
            }
            throw new APIException("Image upload succeeded but no hash returned");

        } catch (APIException e) {
            throw e;
        } catch (Exception e) {
            throw new APIException("Image upload failed: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    /**
     * Creates an AdCreative with link + image for a Facebook Page.
     * Set degreesOfFreedom=true to enroll in Advantage+ Creative (Meta auto-optimises ad elements).
     */
    public static String createAdCreative(APIContext ctx, String adAccountId, String pageId,
            String headline, String body, String description, String imageHash,
            String linkUrl, String callToActionType, boolean degreesOfFreedom) throws APIException {

        AdCreativeLinkDataCallToActionValue ctaValue = new AdCreativeLinkDataCallToActionValue();
        ctaValue.setFieldLink(linkUrl);

        AdCreativeLinkDataCallToAction cta = new AdCreativeLinkDataCallToAction();
        cta.setFieldType(AdCreativeLinkDataCallToAction.EnumType.valueOf("VALUE_" + callToActionType));
        cta.setFieldValue(ctaValue);

        AdCreativeLinkData linkData = new AdCreativeLinkData();
        linkData.setFieldLink(linkUrl);
        linkData.setFieldMessage(body);
        linkData.setFieldName(headline);
        if (description != null && !description.isBlank()) {
            linkData.setFieldDescription(description);
        }
        linkData.setFieldImageHash(imageHash);
        linkData.setFieldCallToAction(cta);

        AdCreativeObjectStorySpec storySpec = new AdCreativeObjectStorySpec();
        storySpec.setFieldPageId(pageId);
        storySpec.setFieldLinkData(linkData);

        AdAccount.APIRequestCreateAdCreative req = new AdAccount(adAccountId, ctx).createAdCreative()
                .setName(headline + " - Creative")
                .setObjectStorySpec(storySpec);

        if (degreesOfFreedom) {
            req.setParam("degrees_of_freedom_spec", buildDegreesOfFreedomSpec());
        }

        return req.execute().getId();
    }

    /**
     * Uploads a video from a public URL to the Meta Ad Account.
     * Downloads to a temp file first (avoids capability restriction on URL uploads).
     */
    public static String uploadVideoFromUrl(APIContext ctx, String adAccountId, String videoUrl) throws APIException {
        File tempFile = null;
        try {
            String ext = videoUrl.contains(".mp4") ? ".mp4" : ".mp4";
            tempFile = Files.createTempFile("caliper_video_", ext).toFile();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(videoUrl))
                    .GET()
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                throw new APIException("Failed to download video (HTTP " + response.statusCode() + "): " + videoUrl);
            }
            try (InputStream in = response.body(); FileOutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }

            AdVideo video = new AdAccount(adAccountId, ctx).createAdVideo()
                    .addUploadFile("source", tempFile)
                    .execute();

            if (video.getId() != null && !video.getId().isEmpty()) {
                return video.getId();
            }
            throw new APIException("Video upload succeeded but no ID returned");

        } catch (APIException e) {
            throw e;
        } catch (Exception e) {
            throw new APIException("Video upload failed: " + e.getMessage());
        } finally {
            if (tempFile != null) tempFile.delete();
        }
    }

    /**
     * Polls Meta's Graph API until the uploaded video reaches "ready" status.
     * Must be called after uploadVideoFromUrl() and before createVideoAdCreative().
     *
     * Meta processes videos asynchronously after upload. Attempting to use a video
     * in a creative while it is still processing causes error_subcode 1885252.
     *
     * @param maxWaitSeconds Maximum total seconds to wait before giving up (suggested: 600)
     * @throws APIException  If video processing fails or the timeout is reached
     * @throws InterruptedException If the thread is interrupted while sleeping
     */
    public static void waitForVideoReady(APIContext ctx, String videoId, long maxWaitSeconds)
            throws Exception {

        String accessToken = ctx.getAccessToken();
        String url = "https://graph.facebook.com/v19.0/" + videoId
                + "?fields=status&access_token=" + accessToken;

        HttpClient client = HttpClient.newHttpClient();
        long pollIntervalMs = 10_000L;
        long maxAttempts = Math.max(1, maxWaitSeconds * 1000L / pollIntervalMs);

        for (long attempt = 1; attempt <= maxAttempts; attempt++) {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            if (json.has("status")) {
                JsonObject status = json.getAsJsonObject("status");
                String videoStatus = status.has("video_status")
                        ? status.get("video_status").getAsString() : "";
                int progress = status.has("processing_progress")
                        ? status.get("processing_progress").getAsInt() : 0;

                if ("ready".equalsIgnoreCase(videoStatus)) {
                    return;
                }
                if ("error".equalsIgnoreCase(videoStatus)) {
                    throw new APIException("Video processing failed for id=" + videoId
                            + ": " + status);
                }
                // Still processing — log progress and wait
                if (attempt < maxAttempts) {
                    Thread.sleep(pollIntervalMs);
                }
            } else {
                // Unexpected response shape — wait and retry
                if (attempt < maxAttempts) Thread.sleep(pollIntervalMs);
            }
        }

        throw new APIException("Video id=" + videoId + " not ready after " + maxWaitSeconds + "s");
    }

    /**
     * Creates a Video AdCreative.
     * Set degreesOfFreedom=true to enroll in Advantage+ Creative.
     */
    public static String createVideoAdCreative(APIContext ctx, String adAccountId, String pageId,
            String title, String body, String description, String videoId,
            String linkUrl, String callToActionType, String thumbnailHash,
            boolean degreesOfFreedom) throws APIException {

        AdCreativeLinkDataCallToActionValue ctaValue = new AdCreativeLinkDataCallToActionValue();
        ctaValue.setFieldLink(linkUrl);

        AdCreativeLinkDataCallToAction cta = new AdCreativeLinkDataCallToAction();
        cta.setFieldType(AdCreativeLinkDataCallToAction.EnumType.valueOf("VALUE_" + callToActionType));
        cta.setFieldValue(ctaValue);

        AdCreativeVideoData videoData = new AdCreativeVideoData();
        videoData.setFieldVideoId(videoId);
        videoData.setFieldTitle(title);
        videoData.setFieldMessage(body);
        if (description != null && !description.isBlank()) {
            videoData.setFieldLinkDescription(description);
        }
        if (thumbnailHash != null && !thumbnailHash.isBlank()) {
            videoData.setFieldImageHash(thumbnailHash);
        }
        videoData.setFieldCallToAction(cta);

        AdCreativeObjectStorySpec storySpec = new AdCreativeObjectStorySpec();
        storySpec.setFieldPageId(pageId);
        storySpec.setFieldVideoData(videoData);

        AdAccount.APIRequestCreateAdCreative req = new AdAccount(adAccountId, ctx).createAdCreative()
                .setName(title + " - Video Creative")
                .setObjectStorySpec(storySpec);

        if (degreesOfFreedom) {
            req.setParam("degrees_of_freedom_spec", buildDegreesOfFreedomSpec());
        }

        return req.execute().getId();
    }

    /**
     * Creates a Carousel AdCreative.
     * Each card in carouselCards must have: imageHash (pre-uploaded), linkUrl, headline, description.
     * carouselCards format: List of Map with keys "imageHash","linkUrl","headline","description"
     * Set degreesOfFreedom=true to enroll in Advantage+ Creative.
     */
    public static String createCarouselAdCreative(APIContext ctx, String adAccountId, String pageId,
            String body, String defaultLinkUrl, String callToActionType,
            List<Map<String, String>> carouselCards, boolean degreesOfFreedom) throws APIException {

        List<AdCreativeLinkDataChildAttachment> attachments = new ArrayList<>();
        for (Map<String, String> card : carouselCards) {
            AdCreativeLinkDataCallToActionValue ctaValue = new AdCreativeLinkDataCallToActionValue();
            ctaValue.setFieldLink(card.getOrDefault("linkUrl", defaultLinkUrl));

            AdCreativeLinkDataCallToAction cta = new AdCreativeLinkDataCallToAction();
            cta.setFieldType(AdCreativeLinkDataCallToAction.EnumType.valueOf("VALUE_" + callToActionType));
            cta.setFieldValue(ctaValue);

            AdCreativeLinkDataChildAttachment attachment = new AdCreativeLinkDataChildAttachment();
            attachment.setFieldLink(card.getOrDefault("linkUrl", defaultLinkUrl));
            attachment.setFieldName(card.getOrDefault("headline", ""));
            attachment.setFieldDescription(card.getOrDefault("description", ""));
            attachment.setFieldImageHash(card.get("imageHash"));
            attachment.setFieldCallToAction(cta);

            attachments.add(attachment);
        }

        AdCreativeLinkData linkData = new AdCreativeLinkData();
        linkData.setFieldLink(defaultLinkUrl);
        linkData.setFieldMessage(body);
        linkData.setFieldChildAttachments(attachments);
        linkData.setFieldMultiShareOptimized(true);
        linkData.setFieldMultiShareEndCard(false);

        AdCreativeObjectStorySpec storySpec = new AdCreativeObjectStorySpec();
        storySpec.setFieldPageId(pageId);
        storySpec.setFieldLinkData(linkData);

        AdAccount.APIRequestCreateAdCreative req = new AdAccount(adAccountId, ctx).createAdCreative()
                .setName("Carousel Creative")
                .setObjectStorySpec(storySpec);

        if (degreesOfFreedom) {
            req.setParam("degrees_of_freedom_spec", buildDegreesOfFreedomSpec());
        }

        return req.execute().getId();
    }

    /**
     * Searches Meta's Targeting Search API for geo locations (cities or regions).
     * Returns list of {key, name, type, country_code, region}.
     * The "key" value is what must be stored and passed to buildCityTargeting / buildRegionTargeting.
     *
     * @param locationType  "city" | "region" | "country" | "zip"
     */
    public static List<Map<String, String>> searchGeoLocations(
            String accessToken, String query, String locationType) throws Exception {

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://graph.facebook.com/v19.0/search?type=adgeolocation"
                + "&q=" + encodedQuery
                + "&location_types=[%22" + locationType + "%22]"
                + "&access_token=" + accessToken;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

        List<Map<String, String>> results = new ArrayList<>();
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        if (json.has("data")) {
            for (JsonElement element : json.getAsJsonArray("data")) {
                JsonObject loc = element.getAsJsonObject();
                Map<String, String> result = new LinkedHashMap<>();
                result.put("key",          geoField(loc, "key"));
                result.put("name",         geoField(loc, "name"));
                result.put("type",         geoField(loc, "type"));
                result.put("country_code", geoField(loc, "country_code"));
                result.put("region",       geoField(loc, "region"));
                results.add(result);
            }
        }
        return results;
    }

    private static String geoField(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : "";
    }

    /**
     * Creates an Ad linking an AdSet to an AdCreative.
     */
    public static String createAd(APIContext ctx, String adAccountId, String adSetId,
            String creativeId, String adName) throws APIException {

        AdCreative creativeRef = new AdCreative(creativeId, ctx);

        Ad ad = new AdAccount(adAccountId, ctx).createAd()
                .setName(adName)
                .setAdsetId(adSetId)
                .setCreative(creativeRef)
                .setStatus(Ad.EnumStatus.VALUE_PAUSED)
                .execute();

        return ad.getId();
    }

    public static void updateCampaignStatus(APIContext ctx, String metaCampaignId, String status) throws APIException {
        new Campaign(metaCampaignId, ctx).update()
                .setStatus(Campaign.EnumStatus.valueOf(status))
                .execute();
    }

    public static void updateAdSetStatus(APIContext ctx, String metaAdSetId, String status) throws APIException {
        new AdSet(metaAdSetId, ctx).update()
                .setStatus(AdSet.EnumStatus.valueOf(status))
                .execute();
    }

    public static void updateAdStatus(APIContext ctx, String metaAdId, String status) throws APIException {
        new Ad(metaAdId, ctx).update()
                .setStatus(Ad.EnumStatus.valueOf(status))
                .execute();
    }

    /**
     * Fetches campaign-level insights from Meta.
     */
    public static APINodeList<AdsInsights> getCampaignInsights(APIContext ctx, String metaCampaignId,
            String datePreset, String[] fields) throws APIException {

        Campaign.APIRequestGetInsights request = new Campaign(metaCampaignId, ctx).getInsights();
        request.setParam("date_preset", datePreset);
        for (String field : fields) {
            request.requestField(field);
        }
        return request.execute();
    }

    /**
     * Fetches full lead data from Meta by lead ID.
     */
    public static Lead fetchLead(APIContext ctx, String leadId) throws APIException {
        return new Lead(leadId, ctx).get()
                .requestField("id")
                .requestField("ad_id")
                .requestField("form_id")
                .requestField("field_data")
                .requestField("created_time")
                .execute();
    }

    public static String[] defaultInsightFields() {
        return new String[]{"impressions", "reach", "clicks", "spend", "actions"};
    }

    // ──────────────── Targeting Builders ──────────────────────────────────────

    public static Targeting buildRadiusTargeting(double latitude, double longitude,
            double radius, String radiusUnit) {

        TargetingGeoLocationCustomLocation customLocation = new TargetingGeoLocationCustomLocation();
        customLocation.setFieldLatitude(latitude);
        customLocation.setFieldLongitude(longitude);
        customLocation.setFieldRadius(radius);
        customLocation.setFieldDistanceUnit("mile".equalsIgnoreCase(radiusUnit) ? "mile" : "kilometer");

        List<TargetingGeoLocationCustomLocation> customLocations = new ArrayList<>();
        customLocations.add(customLocation);

        TargetingGeoLocation geoLocation = new TargetingGeoLocation();
        geoLocation.setFieldCustomLocations(customLocations);

        Targeting targeting = new Targeting();
        targeting.setFieldGeoLocations(geoLocation);
        return targeting;
    }

    /**
     * City targeting using a Meta city key (from Targeting Search API).
     */
    public static Targeting buildCityTargeting(String cityKey) {
        TargetingGeoLocationCity city = new TargetingGeoLocationCity();
        city.setFieldKey(cityKey);

        List<TargetingGeoLocationCity> cities = new ArrayList<>();
        cities.add(city);

        TargetingGeoLocation geoLocation = new TargetingGeoLocation();
        geoLocation.setFieldCities(cities);

        Targeting targeting = new Targeting();
        targeting.setFieldGeoLocations(geoLocation);
        return targeting;
    }

    /**
     * Region targeting using a Meta region key (from Targeting Search API).
     */
    public static Targeting buildRegionTargeting(String regionKey) {
        TargetingGeoLocationRegion region = new TargetingGeoLocationRegion();
        region.setFieldKey(regionKey);

        List<TargetingGeoLocationRegion> regions = new ArrayList<>();
        regions.add(region);

        TargetingGeoLocation geoLocation = new TargetingGeoLocation();
        geoLocation.setFieldRegions(regions);

        Targeting targeting = new Targeting();
        targeting.setFieldGeoLocations(geoLocation);
        return targeting;
    }

    /**
     * Zip/pincode targeting using the postal code as the key.
     */
    public static Targeting buildZipTargeting(String zipKey) {
        TargetingGeoLocationZip zip = new TargetingGeoLocationZip();
        zip.setFieldKey(zipKey);

        List<TargetingGeoLocationZip> zips = new ArrayList<>();
        zips.add(zip);

        TargetingGeoLocation geoLocation = new TargetingGeoLocation();
        geoLocation.setFieldZips(zips);

        Targeting targeting = new Targeting();
        targeting.setFieldGeoLocations(geoLocation);
        return targeting;
    }

    /**
     * Applies audience targeting (gender, age, placements) to an existing Targeting object.
     * All params are optional — pass null/empty to skip.
     *
     * @param gender              "MALE" | "FEMALE" | "ALL" | null (defaults to ALL)
     * @param ageMin              Minimum age (18–65). Pass null to use Meta default (18)
     * @param ageMax              Maximum age (18–65). Pass null to use Meta default (65)
     * @param publisherPlatforms  e.g. ["facebook","instagram","messenger","audience_network"]
     * @param facebookPositions   e.g. ["feed","story","reels","marketplace","video_feeds","search"]
     * @param instagramPositions  e.g. ["stream","story","explore","reels","profile_feed"]
     */
    public static Targeting applyAudienceTargeting(Targeting targeting,
            String gender, Long ageMin, Long ageMax,
            List<String> publisherPlatforms, List<String> facebookPositions,
            List<String> instagramPositions) {

        if (gender != null && !gender.isBlank() && !"ALL".equalsIgnoreCase(gender)) {
            List<Long> genderCodes = new ArrayList<>();
            if ("MALE".equalsIgnoreCase(gender))   genderCodes.add(1L);
            if ("FEMALE".equalsIgnoreCase(gender)) genderCodes.add(2L);
            if (!genderCodes.isEmpty()) targeting.setFieldGenders(genderCodes);
        }

        if (ageMin != null && ageMin > 0) targeting.setFieldAgeMin(ageMin);
        if (ageMax != null && ageMax > 0) targeting.setFieldAgeMax(ageMax);

        if (publisherPlatforms != null && !publisherPlatforms.isEmpty()) {
            targeting.setFieldPublisherPlatforms(publisherPlatforms);
        }
        if (facebookPositions != null && !facebookPositions.isEmpty()) {
            targeting.setFieldFacebookPositions(facebookPositions);
        }
        if (instagramPositions != null && !instagramPositions.isEmpty()) {
            targeting.setFieldInstagramPositions(instagramPositions);
        }
        TargetingAutomation targetingAutomation = new TargetingAutomation();
		targetingAutomation.setFieldAdvantageAudience(0L);
		targeting.setFieldTargetingAutomation(targetingAutomation);
        return targeting;
    }

    /**
     * Builds the AdPromotedObject using the objective / optimization-goal / destination-type matrix.
     *
     * Meta API rules:
     *  OUTCOME_AWARENESS                              → null (no promoted object)
     *  OUTCOME_TRAFFIC  + MESSENGER/INSTAGRAM_DIRECT  → page_id required
     *  OUTCOME_TRAFFIC  + WEBSITE                     → pixel_id + custom_event_type optional
     *  OUTCOME_LEADS    + INSTANT_FORMS/PHONE_CALL    → page_id required
     *  OUTCOME_LEADS    + MESSENGER                   → page_id required
     *  OUTCOME_LEADS    + WEBSITE                     → pixel_id required, event defaults to LEAD
     *  OUTCOME_ENGAGEMENT + POST_ENGAGEMENT / FACEBOOK_PAGE → page_id required
     *  OUTCOME_ENGAGEMENT + other                     → page_id or pixel optional
     *  OUTCOME_SALES    + OFFSITE_CONVERSIONS / VALUE → pixel_id required, event defaults to PURCHASE
     *  OUTCOME_SALES    + other                       → pixel_id + event optional
     *
     * Note: whenever pixel_id is set, custom_event_type is always set (Meta enforces this).
     */
    public static AdPromotedObject buildPromotedObject(
            String objective, String optimizationGoal, String destinationType,
            String pageId, String pixelId, String customEventType, String whatsappNumber) {

        String obj  = safe(objective);
        String goal = safe(optimizationGoal);
        String dest = safe(destinationType);
        String pg   = safe(pageId);
        String px   = safe(pixelId);
        String cet  = safe(customEventType);
        String wa   = safe(whatsappNumber);

        // WhatsApp destination — takes priority across all objectives
        if ("WHATSAPP".equals(dest)) {
            requireField(pg, "page_id", obj + " + WHATSAPP");
            requireField(wa, "whatsapp_number", obj + " + WHATSAPP");
            return poWhatsApp(pg, wa);
        }

        switch (obj) {

            // ── Awareness: promoted_object is never set ───────────────────────
            case "OUTCOME_AWARENESS":
                return null;

            // ── Traffic ───────────────────────────────────────────────────────
            case "OUTCOME_TRAFFIC": {
                if ("MESSENGER".equals(dest) || "INSTAGRAM_DIRECT".equals(dest)) {
                    requireField(pg, "page_id", obj + " + " + dest);
                    return poPage(pg);
                }
                // WEBSITE / default: pixel + event optional, page optional
                return poPixelAndPage(px, pg, cet.isEmpty() ? null : cet);
            }

            // ── Leads ─────────────────────────────────────────────────────────
            case "OUTCOME_LEADS": {
                boolean formBased = "INSTANT_FORMS".equals(dest)
                        || "PHONE_CALL".equals(dest)
                        || "MESSENGER".equals(dest);
                // If dest is empty and goal is LEAD_GENERATION / QUALITY_LEAD, assume INSTANT_FORMS
                boolean assumeForm = dest.isEmpty()
                        && ("LEAD_GENERATION".equals(goal) || "QUALITY_LEAD".equals(goal));

                if (formBased || assumeForm) {
                    requireField(pg, "page_id", obj + " + " + (dest.isEmpty() ? "INSTANT_FORMS" : dest));
                    return poPage(pg);
                }
                // WEBSITE destination: pixel required, event defaults to LEAD
                requireField(px, "pixel_id", obj + " + WEBSITE");
                String event = cet.isEmpty() ? "LEAD" : cet;
                return poPixelAndPage(px, pg, event);
            }

            // ── Engagement ────────────────────────────────────────────────────
            case "OUTCOME_ENGAGEMENT": {
                boolean pageRequired = "POST_ENGAGEMENT".equals(goal) || "FACEBOOK_PAGE".equals(dest);
                if (pageRequired) {
                    requireField(pg, "page_id", obj + " + " + goal);
                    return poPage(pg);
                }
                // VIDEO_VIEWS / REACH / LINK_CLICKS: page/pixel optional
                return poPixelAndPage(px, pg, cet.isEmpty() ? null : cet);
            }

            // ── Sales ─────────────────────────────────────────────────────────
            case "OUTCOME_SALES": {
                if ("OFFSITE_CONVERSIONS".equals(goal) || "VALUE".equals(goal)) {
                    requireField(px, "pixel_id", obj + " + " + goal);
                    String event = cet.isEmpty() ? "PURCHASE" : cet;
                    return poPixelAndPage(px, pg, event);
                }
                // LINK_CLICKS / LANDING_PAGE_VIEWS: pixel + event optional
                return poPixelAndPage(px, pg, cet.isEmpty() ? null : cet);
            }

            default:
                return null;
        }
    }

    // ──────────────── Private Helpers ────────────────────────────────────────

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private static void requireField(String value, String field, String context) {
        if (value.isEmpty()) throw new InvalidRequestException(
                field + " is required for " + context + " but was not provided");
    }

    private static AdPromotedObject poPage(String pageId) {
        return new AdPromotedObject().setFieldPageId(pageId);
    }

    private static AdPromotedObject poWhatsApp(String pageId, String whatsappNumber) {
        AdPromotedObject po = new AdPromotedObject();
        po.setFieldPageId(pageId);
        po.setFieldWhatsappPhoneNumber(whatsappNumber);
        return po;
    }

    /** Builds a promoted object with pixel + optional page. Always sets custom_event_type when pixel is present. */
    private static AdPromotedObject poPixelAndPage(String pixelId, String pageId, String customEventType) {
        if (pixelId.isEmpty() && pageId.isEmpty()) return null;
        AdPromotedObject po = new AdPromotedObject();
        if (!pixelId.isEmpty()) {
            po.setFieldPixelId(pixelId);
            if (customEventType != null && !customEventType.isEmpty()) {
                po.setFieldCustomEventType(
                        AdPromotedObject.EnumCustomEventType.valueOf("VALUE_" + customEventType));
            }
        }
        if (!pageId.isEmpty()) po.setFieldPageId(pageId);
        return po;
    }

    private static List<String> splitComma(String csv) {
        if (csv == null || csv.isBlank()) return new ArrayList<>();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static Campaign.EnumObjective mapObjective(String objective) {
        return switch (objective) {
            case "OUTCOME_AWARENESS"  -> Campaign.EnumObjective.VALUE_OUTCOME_AWARENESS;
            case "OUTCOME_TRAFFIC"    -> Campaign.EnumObjective.VALUE_OUTCOME_TRAFFIC;
            case "OUTCOME_LEADS"      -> Campaign.EnumObjective.VALUE_OUTCOME_LEADS;
            case "OUTCOME_ENGAGEMENT" -> Campaign.EnumObjective.VALUE_OUTCOME_ENGAGEMENT;
            case "OUTCOME_SALES"      -> Campaign.EnumObjective.VALUE_OUTCOME_SALES;
            default -> throw new InvalidRequestException("Unsupported Meta campaign objective: " + objective);
        };
    }

    /**
     * Returns the degrees_of_freedom_spec JSON string for Advantage+ Creative enrollment.
     * When set, Meta is allowed to automatically enhance ad elements (text, images, CTA).
     */
    private static String buildDegreesOfFreedomSpec() {
        return "{\"creative_features_spec\":{\"standard_enhancements\":{\"enroll_status\":\"OPT_IN\"}}}";
    }

    /**
     * Creates an AdCreative using asset_feed_spec with multiple images (one per aspect ratio).
     * Meta automatically selects the best-fitting image for each ad placement.
     *
     * @param imageHashes  Pre-uploaded image hashes (at least one required)
     * @param degreesOfFreedom  True to enroll in Advantage+ Creative
     */
    public static String createAssetFeedAdCreative(APIContext ctx, String adAccountId, String pageId,
            String headline, String body, String description,
            List<String> imageHashes, String linkUrl, String callToActionType,
            boolean degreesOfFreedom) throws APIException {

        JsonObject assetFeedSpec = new JsonObject();

        JsonArray images = new JsonArray();
        for (String hash : imageHashes) {
            JsonObject img = new JsonObject();
            img.addProperty("hash", hash);
            images.add(img);
        }
        assetFeedSpec.add("images", images);

        JsonArray titles = new JsonArray();
        JsonObject titleObj = new JsonObject();
        titleObj.addProperty("text", headline);
        titles.add(titleObj);
        assetFeedSpec.add("titles", titles);

        JsonArray bodies = new JsonArray();
        JsonObject bodyObj = new JsonObject();
        bodyObj.addProperty("text", body);
        bodies.add(bodyObj);
        assetFeedSpec.add("bodies", bodies);

        JsonArray linkUrls = new JsonArray();
        JsonObject linkUrlObj = new JsonObject();
        linkUrlObj.addProperty("website_url", linkUrl);
        linkUrls.add(linkUrlObj);
        assetFeedSpec.add("link_urls", linkUrls);

        JsonArray ctas = new JsonArray();
        ctas.add(callToActionType);
        assetFeedSpec.add("call_to_action_types", ctas);

        if (description != null && !description.isBlank()) {
            JsonArray descs = new JsonArray();
            JsonObject descObj = new JsonObject();
            descObj.addProperty("text", description);
            descs.add(descObj);
            assetFeedSpec.add("descriptions", descs);
        }

        AdAccount.APIRequestCreateAdCreative req = new AdAccount(adAccountId, ctx).createAdCreative()
                .setName(headline + " - Multi-Image Creative")
                .setParam("actor_id", pageId)
                .setParam("object_type", "SHARE")
                .setParam("asset_feed_spec", assetFeedSpec.toString());

        if (degreesOfFreedom) {
            req.setParam("degrees_of_freedom_spec", buildDegreesOfFreedomSpec());
        }

        return req.execute().getId();
    }
}
