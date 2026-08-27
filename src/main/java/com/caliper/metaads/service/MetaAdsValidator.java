package com.caliper.metaads.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.caliper.campaign.facebook.dto.request.CarouselCardDto;
import com.caliper.campaign.facebook.dto.request.ClientMetaCampaignDetailsDto;
import com.caliper.campaign.facebook.dto.request.ImageAssetDto;
import com.caliper.campaign.facebook.entity.MetaAdCreative;
import com.caliper.campaign.facebook.entity.MetaCampaign;
import com.caliper.utils.exception.customException.InvalidRequestException;

public class MetaAdsValidator {

    private static final Set<String> VALID_OBJECTIVES = Set.of(
            "OUTCOME_AWARENESS", "OUTCOME_TRAFFIC", "OUTCOME_LEADS",
            "OUTCOME_ENGAGEMENT", "OUTCOME_SALES");

    private static final BigDecimal MIN_DAILY_BUDGET = new BigDecimal("40");

    private MetaAdsValidator() {}

    public static void validateCreateCampaign(ClientMetaCampaignDetailsDto req) {
        // Objective
        if (!VALID_OBJECTIVES.contains(req.getObjective())) {
            throw new InvalidRequestException("Invalid objective: " + req.getObjective()
                    + ". Must be one of: " + VALID_OBJECTIVES);
        }

        // Dates
        Date now = new Date();
        if (req.getStartDate() == null) throw new InvalidRequestException("start_date is required");
        if (req.getEndDate()   == null) throw new InvalidRequestException("end_date is required");
        if (!req.getEndDate().after(req.getStartDate())) {
            throw new InvalidRequestException("end_date must be after start_date");
        }

        // Budget
        String budgetType = req.getBudgetType() != null ? req.getBudgetType() : MetaCampaign.BUDGET_TYPE_DAILY;
        if (MetaCampaign.BUDGET_TYPE_LIFETIME.equalsIgnoreCase(budgetType)) {
            if (req.getTotalBudget() == null || req.getTotalBudget().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidRequestException("total_budget must be > 0 for LIFETIME budget type");
            }
        } else {
            if (req.getDailyBudget() == null || req.getDailyBudget().compareTo(MIN_DAILY_BUDGET) < 0) {
                throw new InvalidRequestException(
                        "daily_budget must be at least " + MIN_DAILY_BUDGET + " for DAILY budget type");
            }
        }

        // Creative type
        String creativeType = req.getCreativeType() != null
                ? req.getCreativeType() : MetaAdCreative.CREATIVE_TYPE_IMAGE;

        switch (creativeType) {
            case MetaAdCreative.CREATIVE_TYPE_IMAGE -> {
                boolean hasImageUrl = req.getImageUrl() != null && !req.getImageUrl().isBlank();
                boolean hasImageAssets = req.getImageAssets() != null && !req.getImageAssets().isEmpty();
                if (!hasImageUrl && !hasImageAssets) {
                    throw new InvalidRequestException(
                            "image_url or image_assets is required for IMAGE creative type");
                }
                if (hasImageAssets) {
                    List<ImageAssetDto> assets = req.getImageAssets();
                    for (int i = 0; i < assets.size(); i++) {
                        if (assets.get(i).getImageUrl() == null || assets.get(i).getImageUrl().isBlank()) {
                            throw new InvalidRequestException(
                                    "image_assets[" + i + "].image_url is required");
                        }
                    }
                }
            }
            case MetaAdCreative.CREATIVE_TYPE_VIDEO -> {
                if (req.getVideoUrl() == null || req.getVideoUrl().isBlank()) {
                    throw new InvalidRequestException("video_url is required for VIDEO creative type");
                }
                if (req.getThumbnailUrl() == null || req.getThumbnailUrl().isBlank()) {
                    throw new InvalidRequestException("thumbnail_url is required for VIDEO creative type (Meta requires a video thumbnail)");
                }
            }
            case MetaAdCreative.CREATIVE_TYPE_CAROUSEL -> {
                List<CarouselCardDto> cards = req.getCarouselCards();
                if (cards == null || cards.size() < 2) {
                    throw new InvalidRequestException(
                            "carousel_cards must contain at least 2 cards for CAROUSEL creative type");
                }
                for (int i = 0; i < cards.size(); i++) {
                    CarouselCardDto card = cards.get(i);
                    if (card.getImageUrl() == null || card.getImageUrl().isBlank()) {
                        throw new InvalidRequestException("carousel card[" + i + "] image_url is required");
                    }
                    if (card.getLinkUrl() == null || card.getLinkUrl().isBlank()) {
                        throw new InvalidRequestException("carousel card[" + i + "] link_url is required");
                    }
                }
            }
            default -> throw new InvalidRequestException(
                    "Invalid creative_type: " + creativeType + ". Must be IMAGE, VIDEO, or CAROUSEL");
        }

        // Age range
        if (req.getAgeMin() != null && req.getAgeMax() != null) {
            if (req.getAgeMin() < 18) throw new InvalidRequestException("age_min must be >= 18");
            if (req.getAgeMax() > 65) throw new InvalidRequestException("age_max must be <= 65");
            if (req.getAgeMin() > req.getAgeMax()) {
                throw new InvalidRequestException("age_min must be <= age_max");
            }
        }

        // WhatsApp destination
        if ("WHATSAPP".equals(req.getDestinationType())) {
            if (req.getWhatsappNumber() == null || req.getWhatsappNumber().isBlank()) {
                throw new InvalidRequestException(
                        "whatsapp_number is required when destination_type is WHATSAPP. "
                        + "Include country code e.g. \"+919876543210\"");
            }
        }

        // Geo targeting
        String targetingType = req.getTargetingType();
        if ("PINCODE".equals(targetingType) && (req.getPincode() == null || req.getPincode().isBlank())) {
            throw new InvalidRequestException("pincode is required when targeting_type is PINCODE");
        }
        if ("CITY".equals(targetingType) && (req.getCity() == null || req.getCity().isBlank())) {
            throw new InvalidRequestException("city name is required when targeting_type is CITY. "
                    + "Use GET /meta-campaign/geo-search?q=Mumbai&location_type=city to browse available cities.");
        }
        if ("REGION".equals(targetingType) && (req.getRegion() == null || req.getRegion().isBlank())) {
            throw new InvalidRequestException("region name is required when targeting_type is REGION. "
                    + "Use GET /meta-campaign/geo-search?location_type=region to browse available regions.");
        }
    }
}
