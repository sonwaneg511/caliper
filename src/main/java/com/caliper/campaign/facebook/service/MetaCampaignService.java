package com.caliper.campaign.facebook.service;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caliper.campaign.facebook.dto.request.CarouselCardDto;
import com.caliper.campaign.facebook.dto.request.ClientMetaCampaignDetailsDto;
import com.caliper.campaign.facebook.dto.request.ImageAssetDto;
import com.caliper.campaign.facebook.dto.request.CoeMetaCampaignDetailsDto;
import com.caliper.campaign.facebook.dto.request.MetaCampaignFilterRequest;
import com.caliper.campaign.facebook.dto.response.ViewMetaCampaignDetailsResponse;
import com.caliper.campaign.facebook.dto.response.ViewMetaCampaignResponse;
import com.caliper.campaign.facebook.entity.MetaAd;
import com.caliper.campaign.facebook.entity.MetaAdCarouselCard;
import com.caliper.campaign.facebook.entity.MetaAdCreative;
import com.caliper.campaign.facebook.entity.MetaAdImage;
import com.caliper.campaign.facebook.entity.MetaAdImageAsset;
import com.caliper.campaign.facebook.entity.MetaAdSet;
import com.caliper.campaign.facebook.entity.MetaCampaign;
import com.caliper.campaign.facebook.entity.MetaGeoLocation;
import com.caliper.campaign.facebook.repository.MetaAdCarouselCardRepository;
import com.caliper.campaign.facebook.repository.MetaAdCreativeRepository;
import com.caliper.campaign.facebook.repository.MetaAdImageAssetRepository;
import com.caliper.campaign.facebook.repository.MetaAdImageRepository;
import com.caliper.campaign.facebook.repository.MetaAdRepository;
import com.caliper.campaign.facebook.repository.MetaAdSetRepository;
import com.caliper.campaign.facebook.repository.MetaCampaignRepository;
import com.caliper.campaign.facebook.repository.MetaGeoLocationRepository;
import com.caliper.campaign.google.dto.response.SelfServeResponse;
import com.caliper.campaign.google.entity.ClientLocationSetup;
import com.caliper.campaign.google.repository.ClientLocationSetupRepository;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.facebook.entity.FacebookAccount;
import com.caliper.location.facebook.entity.FacebookPage;
import com.caliper.location.facebook.repository.FacebookAccountRepository;
import com.caliper.location.facebook.repository.FacebookPageRepository;
import com.caliper.location.repository.DealerLocationRepository;
import com.caliper.metaads.service.MetaAdsApiService;
import com.caliper.metaads.service.MetaAdsValidator;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;

@Service
public class MetaCampaignService {

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
    private MetaAdCarouselCardRepository metaAdCarouselCardRepository;

    @Autowired
    private MetaAdImageAssetRepository metaAdImageAssetRepository;

    @Autowired
    private ClientLocationSetupRepository clientLocationSetupRepository;

    @Autowired
    private FacebookPageRepository facebookPageRepository;

    @Autowired
    private FacebookAccountRepository facebookAccountRepository;

    @Autowired
    private DealerLocationRepository dealerLocationRepository;

    @Autowired
    private MetaGeoLocationRepository metaGeoLocationRepository;

    @Transactional
    public SelfServeResponse createClientMetaCampaign(ClientMetaCampaignDetailsDto request) {

        MetaAdsValidator.validateCreateCampaign(request);

        String clientId = request.getClientId();

        String dealerId = request.getDealerId();

        // ── Auto-fetch Facebook Page (dealer-specific first, shared fallback) ──
        FacebookPage fbPage = facebookPageRepository
                .findByClientIdAndDealerId(clientId, dealerId)
                .or(() -> facebookPageRepository.findByClientIdAndDealerIdIsNull(clientId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No Facebook Page found for client: " + clientId + " dealer: " + dealerId));
        String pageId = String.valueOf(fbPage.getFacebookPageId());

        // ── Auto-fetch Facebook Account (dealer-specific first, shared fallback)
        FacebookAccount account = facebookAccountRepository
                .findByClientIdAndDealerId(clientId, dealerId)
                .or(() -> facebookAccountRepository.findByClientIdAndDealerIdIsNull(clientId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No Facebook Account found for client: " + clientId + " dealer: " + dealerId));
        String pixelId = account.getPixelId() != null ? String.valueOf(account.getPixelId()) : null;

        // ── Resolve destination type (user override takes priority) ───────────
        String destinationType = (request.getDestinationType() != null && !request.getDestinationType().isBlank())
                ? request.getDestinationType()
                : resolveDestinationType(request.getObjective());

        // ── Resolve optimization goal (allow override) ────────────────────────
        String optimizationGoal = (request.getOptimizationGoal() != null && !request.getOptimizationGoal().isBlank())
                ? request.getOptimizationGoal()
                : resolveOptimizationGoal(request.getObjective());

        // ── Auto-derive custom event type ─────────────────────────────────────
        String customEventType = resolveCustomEventType(request.getObjective(), optimizationGoal, destinationType);

        String budgetType = request.getBudgetType() != null ? request.getBudgetType() : MetaCampaign.BUDGET_TYPE_DAILY;
        String cta = request.getCallToActionType() != null ? request.getCallToActionType() : MetaAdCreative.CTA_LEARN_MORE;

        // ── Resolve creative type ─────────────────────────────────────────────
        String creativeType = (request.getCreativeType() != null && !request.getCreativeType().isBlank())
                ? request.getCreativeType()
                : MetaAdCreative.CREATIVE_TYPE_IMAGE;

        MetaCampaign campaign = MetaCampaign.builder()
                .clientId(clientId)
                .dealerId(request.getDealerId())
                .campaignName(request.getCampaignName())
                .metaCampaignId("-1")
                .objective(request.getObjective())
                .dailyBudget(request.getDailyBudget())
                .totalBudget(request.getTotalBudget())
                .budgetType(budgetType)
                .startTime(request.getStartDate())
                .stopTime(request.getEndDate())
                .status(MetaCampaign.STATUS_DRAFT)
                .comment("")
                .coeComment("")
                .errorComment("")
                .clientComment(request.getClientComment() != null ? request.getClientComment() : "")
                .lastModifiedDate(new Date())
                .lastModifiedBy("CLIENT")
                .createdBy(request.getCreatedBy())
                .destinationType(destinationType)
                .build();

        metaCampaignRepository.save(campaign);
        long campaignId = campaign.getId();

        MetaAdSet adSet = buildAdSet(request, campaignId, optimizationGoal, pageId, pixelId, customEventType);
        metaAdSetRepository.save(adSet);
        long adSetId = adSet.getId();

        MetaAdCreative creative = MetaAdCreative.builder()
                .campaignId(campaignId)
                .metaCreativeId("-1")
                .name(request.getCampaignName() + " - Creative")
                .pageId(pageId)
                .headline(request.getHeadline())
                .body(request.getBody())
                .description(request.getDescription() != null ? request.getDescription() : "")
                .callToActionType(cta)
                .linkUrl(request.getLinkUrl())
                .imageUrl(MetaAdCreative.CREATIVE_TYPE_IMAGE.equals(creativeType) ? request.getImageUrl() : null)
                .imageHash("-1")
                .videoUrl(MetaAdCreative.CREATIVE_TYPE_VIDEO.equals(creativeType) ? request.getVideoUrl() : null)
                .videoId("-1")
                .thumbnailUrl(MetaAdCreative.CREATIVE_TYPE_VIDEO.equals(creativeType) ? request.getThumbnailUrl() : null)
                .thumbnailHash(MetaAdCreative.CREATIVE_TYPE_VIDEO.equals(creativeType) ? "-1" : null)
                .creativeType(creativeType)
                .degreesOfFreedom(request.isDegreesOfFreedom())
                .build();

        metaAdCreativeRepository.save(creative);
        long creativeId = creative.getId();

        // For CAROUSEL: save carousel cards
        if (MetaAdCreative.CREATIVE_TYPE_CAROUSEL.equals(creativeType) && request.getCarouselCards() != null) {
            List<CarouselCardDto> cards = request.getCarouselCards();
            for (int i = 0; i < cards.size(); i++) {
                CarouselCardDto card = cards.get(i);
                metaAdCarouselCardRepository.save(MetaAdCarouselCard.builder()
                        .campaignId(campaignId)
                        .creativeId(creativeId)
                        .cardOrder(i)
                        .headline(card.getHeadline())
                        .description(card.getDescription() != null ? card.getDescription() : "")
                        .imageUrl(card.getImageUrl())
                        .imageHash("-1")
                        .linkUrl(card.getLinkUrl())
                        .build());
            }
        }

        MetaAd ad = MetaAd.builder()
                .adSetId(adSetId)
                .campaignId(campaignId)
                .adName(request.getCampaignName() + " - Ad")
                .metaAdId("-1")
                .creativeId(creativeId)
                .status(MetaCampaign.STATUS_DRAFT)
                .build();

        metaAdRepository.save(ad);

        boolean hasImageAssets = request.getImageAssets() != null && !request.getImageAssets().isEmpty();

        if (MetaAdCreative.CREATIVE_TYPE_IMAGE.equals(creativeType)) {
            if (hasImageAssets) {
                // Multi-image path: save each asset with its ratio
                List<ImageAssetDto> assets = request.getImageAssets();
                for (int i = 0; i < assets.size(); i++) {
                    ImageAssetDto asset = assets.get(i);
                    metaAdImageAssetRepository.save(MetaAdImageAsset.builder()
                            .campaignId(campaignId)
                            .creativeId(creativeId)
                            .imageUrl(asset.getImageUrl())
                            .imageHash("-1")
                            .ratio(asset.getRatio())
                            .assetOrder(i)
                            .build());
                }
            } else {
                // Single image path
                MetaAdImage adImage = MetaAdImage.builder()
                        .campaignId(campaignId)
                        .clientId(request.getClientId())
                        .imageUrl(request.getImageUrl())
                        .imageHash("-1")
                        .imageName("campaign_" + campaignId + "_image")
                        .status(MetaAdImage.STATUS_PENDING)
                        .build();
                metaAdImageRepository.save(adImage);
            }
        }

        return new SelfServeResponse(SelfServeResponse.RESULT_SUCCESS,
                "Meta campaign created with id: " + campaignId,
                MetaCampaign.ROLE_CLIENT, campaignId);
    }

    @Transactional
    public SelfServeResponse approveCampaign(CoeMetaCampaignDetailsDto request) {
        MetaCampaign campaign = metaCampaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException("Meta campaign not found: " + request.getCampaignId()));

        if (MetaCampaign.STATUS_DEPLOYED.equals(campaign.getStatus())) {
            throw new InvalidRequestException("Campaign is already deployed: " + request.getCampaignId());
        }

        campaign.setStatus(MetaCampaign.STATUS_PENDING_DEPLOYMENT);
        campaign.setLastModifiedDate(new Date());
        campaign.setLastModifiedBy("HUB_USER");
        if (request.getComment() != null) {
            campaign.setCoeComment(request.getComment());
        }

        metaCampaignRepository.save(campaign);

        return new SelfServeResponse(SelfServeResponse.RESULT_SUCCESS,
                "Campaign approved for deployment: " + request.getCampaignId(),
                MetaCampaign.ROLE_HUB_USER, request.getCampaignId());
    }

    public List<MetaCampaign> findAllByStatus(String status) {
        return metaCampaignRepository.findAllByStatus(status);
    }

    public Page<ViewMetaCampaignResponse> viewAllCampaigns(MetaCampaignFilterRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNo(), 10);
        Page<MetaCampaign> campaigns = metaCampaignRepository
                .findByClientIdAndDealerIdInOrderByIdDesc(
                        request.getClientId(), request.getDealerId(), pageable);

        return campaigns.map(c -> ViewMetaCampaignResponse.builder()
                .campaignId(c.getId())
                .campaignName(c.getCampaignName())
                .objective(c.getObjective())
                .status(c.getStatus())
                .dailyBudget(c.getDailyBudget())
                .totalBudget(c.getTotalBudget())
                .startTime(c.getStartTime())
                .stopTime(c.getStopTime())
                .dealerId(c.getDealerId())
                .metaCampaignId(c.getMetaCampaignId())
                .coeComment(c.getCoeComment())
                .errorComment(c.getErrorComment())
                .clientComment(c.getClientComment())
                .createdBy(c.getCreatedBy())
                .build());
    }

    public ViewMetaCampaignDetailsResponse getCampaignDetails(Long campaignId) {
        MetaCampaign campaign = metaCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta campaign not found: " + campaignId));

        MetaAdSet adSet = metaAdSetRepository.findFirstByCampaignId(campaignId).orElse(null);
        MetaAdCreative creative = metaAdCreativeRepository.findFirstByCampaignId(campaignId).orElse(null);
        MetaAd ad = metaAdRepository.findByCampaignId(campaignId).stream().findFirst().orElse(null);

        return ViewMetaCampaignDetailsResponse.builder()
                .campaignId(campaign.getId())
                .campaignName(campaign.getCampaignName())
                .objective(campaign.getObjective())
                .status(campaign.getStatus())
                .dailyBudget(campaign.getDailyBudget())
                .totalBudget(campaign.getTotalBudget())
                .budgetType(campaign.getBudgetType())
                .startTime(campaign.getStartTime())
                .stopTime(campaign.getStopTime())
                .dealerId(campaign.getDealerId())
                .metaCampaignId(campaign.getMetaCampaignId())
                .coeComment(campaign.getCoeComment())
                .errorComment(campaign.getErrorComment())
                .clientComment(campaign.getClientComment())
                .createdBy(campaign.getCreatedBy())
                .adSet(adSet)
                .adCreative(creative)
                .ad(ad)
                .build();
    }

    public Map<String, Object> getObjectiveOptions() {
        Map<String, Object> options = new LinkedHashMap<>();

        options.put("OUTCOME_AWARENESS", Map.of(
                "optimization_goals", List.of("REACH", "IMPRESSIONS", "AD_RECALL_LIFT", "THRUPLAY"),
                "destination_types", List.of()
        ));
        options.put("OUTCOME_TRAFFIC", Map.of(
                "optimization_goals", List.of("LANDING_PAGE_VIEWS", "LINK_CLICKS", "IMPRESSIONS", "REACH", "POST_ENGAGEMENT"),
                "destination_types", List.of("WEBSITE", "APP", "MESSENGER", "INSTAGRAM_DIRECT", "WHATSAPP")
        ));
        options.put("OUTCOME_LEADS", Map.of(
                "optimization_goals", List.of("LEAD_GENERATION", "QUALITY_LEAD", "LINK_CLICKS", "LANDING_PAGE_VIEWS"),
                "destination_types", List.of("INSTANT_FORMS", "WEBSITE", "MESSENGER", "PHONE_CALL", "WHATSAPP")
        ));
        options.put("OUTCOME_ENGAGEMENT", Map.of(
                "optimization_goals", List.of("POST_ENGAGEMENT", "VIDEO_VIEWS", "REACH", "THRUPLAY", "LINK_CLICKS"),
                "destination_types", List.of("FACEBOOK_PAGE", "WEBSITE", "APP", "MESSENGER", "WHATSAPP")
        ));
        options.put("OUTCOME_SALES", Map.of(
                "optimization_goals", List.of("OFFSITE_CONVERSIONS", "VALUE", "LINK_CLICKS", "LANDING_PAGE_VIEWS"),
                "destination_types", List.of("WEBSITE", "APP", "MESSENGER", "WHATSAPP")
        ));

        return options;
    }

    public List<Map<String, String>> searchGeoLocations(String clientId, String query, String locationType) {
        List<MetaGeoLocation> rows = query == null || query.isBlank()
                ? metaGeoLocationRepository.findByLocationTypeOrderByNameAsc(locationType)
                : metaGeoLocationRepository
                        .findByNameContainingIgnoreCaseAndLocationTypeOrderByNameAsc(query.trim(), locationType);

        return rows.stream().map(r -> {
            Map<String, String> m = new java.util.LinkedHashMap<>();
            m.put("key",          r.getMetaKey());
            m.put("name",         r.getName());
            m.put("type",         r.getLocationType());
            m.put("country_code", r.getCountryCode() != null ? r.getCountryCode() : "");
            m.put("region",       r.getRegionName()  != null ? r.getRegionName()  : "");
            return m;
        }).collect(java.util.stream.Collectors.toList());
    }

    // ──────────────── Helpers ────────────────

    private MetaAdSet buildAdSet(ClientMetaCampaignDetailsDto request, long campaignId,
            String optimizationGoal, String pageId, String pixelId, String customEventType) {
        String targetingType = request.getTargetingType();

        MetaAdSet.MetaAdSetBuilder builder = MetaAdSet.builder()
                .campaignId(campaignId)
                .adSetName(request.getCampaignName() + " - AdSet")
                .metaAdSetId("-1")
                .optimizationGoal(optimizationGoal)
                .billingEvent(MetaAdSet.BILLING_EVENT_IMPRESSIONS)
                .bidAmount(0L)
                .startTime(request.getStartDate())
                .stopTime(request.getEndDate())
                .targetingType(targetingType)
                .status(MetaCampaign.STATUS_DRAFT)
                .bidStrategy(request.getBidStrategy())
                .gender(request.getGender())
                .ageMin(request.getAgeMin())
                .ageMax(request.getAgeMax())
                .publisherPlatforms(joinList(request.getPublisherPlatforms()))
                .facebookPositions(joinList(request.getFacebookPositions()))
                .instagramPositions(joinList(request.getInstagramPositions()))
                .promotedObjectPageId(pageId)
                .promotedObjectPixelId(pixelId)
                .promotedObjectCustomEventType(customEventType)
                .whatsappNumber(request.getWhatsappNumber());

        switch (targetingType) {
            case MetaAdSet.TARGETING_TYPE_PINCODE -> {
                if (request.getPincode() == null || request.getPincode().isBlank()) {
                    throw new InvalidRequestException("pincode is required when targeting_type is PINCODE");
                }
                DealerLocation dealerLoc = dealerLocationRepository
                        .getDealerLocationByDealerIdAndClientId(request.getDealerId(), request.getClientId());
                String countryCode = (dealerLoc != null && dealerLoc.getCountryCode() != null
                        && !dealerLoc.getCountryCode().isBlank())
                        ? dealerLoc.getCountryCode().trim().toUpperCase()
                        : "IN";
                builder.pincode(countryCode + ":" + request.getPincode().trim());
            }
            case MetaAdSet.TARGETING_TYPE_CITY -> {
                if (request.getCity() == null || request.getCity().isBlank()) {
                    throw new InvalidRequestException("city is required when targeting_type is CITY");
                }
                MetaGeoLocation cityGeo = metaGeoLocationRepository
                        .findFirstByNameIgnoreCaseAndLocationType(
                                request.getCity().trim(), MetaGeoLocation.TYPE_CITY)
                        .orElseThrow(() -> new InvalidRequestException(
                                "City '" + request.getCity() + "' not found in the geo-location database. "
                                + "Run the MetaGeoLocationSyncTask first, or search via "
                                + "GET /meta-campaign/geo-search?q=" + request.getCity() + "&location_type=city"));
                builder.city(cityGeo.getMetaKey());
            }
            case MetaAdSet.TARGETING_TYPE_REGION -> {
                if (request.getRegion() == null || request.getRegion().isBlank()) {
                    throw new InvalidRequestException("region is required when targeting_type is REGION");
                }
                MetaGeoLocation regionGeo = metaGeoLocationRepository
                        .findFirstByNameIgnoreCaseAndLocationType(
                                request.getRegion().trim(), MetaGeoLocation.TYPE_REGION)
                        .orElseThrow(() -> new InvalidRequestException(
                                "Region '" + request.getRegion() + "' not found in the geo-location database. "
                                + "Run the MetaGeoLocationSyncTask first, or search via "
                                + "GET /meta-campaign/geo-search?q=" + request.getRegion() + "&location_type=region"));
                builder.region(regionGeo.getMetaKey());
            }
            default -> {
                // RADIUS — pull from ClientLocationSetup
                ClientLocationSetup locationSetup = clientLocationSetupRepository
                        .findByClientIdAndDealerId(request.getClientId(), request.getDealerId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Client location setup not found for dealer: " + request.getDealerId()));
                builder.latitude(locationSetup.getLatitude())
                        .longitude(locationSetup.getLongitude())
                        .radius(locationSetup.getRadius())
                        .radiusUnit(normalizeRadiusUnit(locationSetup.getRadiusUnit()));
            }
        }

        return builder.build();
    }

    private String resolveOptimizationGoal(String objective) {
        return switch (objective) {
            case MetaCampaign.OBJECTIVE_AWARENESS  -> MetaAdSet.OPTIMIZATION_GOAL_REACH;
            case MetaCampaign.OBJECTIVE_TRAFFIC    -> MetaAdSet.OPTIMIZATION_GOAL_LANDING_PAGE_VIEWS;
            case MetaCampaign.OBJECTIVE_LEADS      -> MetaAdSet.OPTIMIZATION_GOAL_LEAD_GENERATION;
            case MetaCampaign.OBJECTIVE_ENGAGEMENT -> MetaAdSet.OPTIMIZATION_GOAL_POST_ENGAGEMENT;
            case MetaCampaign.OBJECTIVE_SALES      -> MetaAdSet.OPTIMIZATION_GOAL_OFFSITE_CONVERSIONS;
            default -> MetaAdSet.OPTIMIZATION_GOAL_REACH;
        };
    }

    /**
     * Auto-derives destination_type from the campaign objective.
     *
     * AWARENESS  → null  (Meta requires no destination for awareness)
     * TRAFFIC    → WEBSITE  (drive visitors to the dealership website)
     * LEADS      → INSTANT_FORMS  (on-Facebook lead form; no website pixel required)
     * ENGAGEMENT → FACEBOOK_PAGE  (page likes / post engagement)
     * SALES      → WEBSITE  (pixel-based purchase conversions)
     */
    private String resolveDestinationType(String objective) {
        return switch (objective) {
            case MetaCampaign.OBJECTIVE_AWARENESS  -> null;
            case MetaCampaign.OBJECTIVE_TRAFFIC    -> "WEBSITE";
            case MetaCampaign.OBJECTIVE_LEADS      -> "INSTANT_FORMS";
            case MetaCampaign.OBJECTIVE_ENGAGEMENT -> "FACEBOOK_PAGE";
            case MetaCampaign.OBJECTIVE_SALES      -> "WEBSITE";
            default -> "WEBSITE";
        };
    }

    /**
     * Auto-derives the custom_event_type for the promoted object's pixel.
     * Only relevant when a pixel is in play (TRAFFIC/SALES to WEBSITE).
     *
     * SALES + OFFSITE_CONVERSIONS / VALUE  → PURCHASE
     * LEADS + WEBSITE                       → LEAD  (though INSTANT_FORMS is the default destination,
     *                                                 keeping LEAD as fallback if dest is overridden)
     * All other combinations               → null (no event type needed)
     */
    private String resolveCustomEventType(String objective, String optimizationGoal, String destinationType) {
        // WhatsApp uses phone number, not a pixel event
        if (MetaAdSet.DESTINATION_TYPE_WHATSAPP.equals(destinationType)) return null;

        if (MetaCampaign.OBJECTIVE_SALES.equals(objective)
                && (MetaAdSet.OPTIMIZATION_GOAL_OFFSITE_CONVERSIONS.equals(optimizationGoal)
                        || "VALUE".equals(optimizationGoal))) {
            return "PURCHASE";
        }
        if (MetaCampaign.OBJECTIVE_LEADS.equals(objective) && "WEBSITE".equals(destinationType)) {
            return "LEAD";
        }
        return null;
    }

    private String joinList(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }

    private String normalizeRadiusUnit(String unit) {
        if (unit == null) return MetaAdSet.RADIUS_UNIT_KILOMETER;
        return "MILES".equalsIgnoreCase(unit) || "mile".equalsIgnoreCase(unit)
                ? MetaAdSet.RADIUS_UNIT_MILE
                : MetaAdSet.RADIUS_UNIT_KILOMETER;
    }
}
