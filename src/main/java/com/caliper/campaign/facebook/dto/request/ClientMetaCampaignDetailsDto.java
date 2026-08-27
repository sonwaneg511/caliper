package com.caliper.campaign.facebook.dto.request;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientMetaCampaignDetailsDto {

    @NotBlank
    @JsonProperty("client_id")
    private String clientId;

    @NotBlank
    @JsonProperty("dealer_id")
    private String dealerId;

    @NotBlank
    @JsonProperty("campaign_name")
    private String campaignName;

    @NotBlank
    @JsonProperty("objective")
    private String objective;

    @NotNull
    @JsonProperty("start_date")
    private Date startDate;

    @NotNull
    @JsonProperty("end_date")
    private Date endDate;

    @NotNull
    @JsonProperty("daily_budget")
    private BigDecimal dailyBudget;

    @JsonProperty("total_budget")
    private BigDecimal totalBudget;

    @JsonProperty("budget_type")
    private String budgetType;

    @NotBlank
    @JsonProperty("headline")
    private String headline;

    @NotBlank
    @JsonProperty("body")
    private String body;

    @JsonProperty("description")
    private String description;

    @JsonProperty("call_to_action_type")
    private String callToActionType;

    @NotBlank
    @JsonProperty("link_url")
    private String linkUrl;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("client_comment")
    private String clientComment;

    @NotBlank
    @JsonProperty("created_by")
    private String createdBy;

    @NotBlank
    @JsonProperty("targeting_type")
    private String targetingType;

    @JsonProperty("pincode")
    private String pincode;

    @JsonProperty("city")
    private String city;

    @JsonProperty("region")
    private String region;

    @JsonProperty("optimization_goal")
    private String optimizationGoal;

    // ── Destination / WhatsApp ────────────────────────────────────────────────
    @JsonProperty("destination_type")
    private String destinationType;   // optional override; required when using WHATSAPP

    @JsonProperty("whatsapp_number")
    private String whatsappNumber;    // e.g. "+919876543210" — required when destination_type is WHATSAPP

    // ── Bid Strategy ──────────────────────────────────────────────────────────
    @JsonProperty("bid_strategy")
    private String bidStrategy;

    // ── Audience Targeting ────────────────────────────────────────────────────
    @JsonProperty("gender")
    private String gender;

    @JsonProperty("age_min")
    private Long ageMin;

    @JsonProperty("age_max")
    private Long ageMax;

    @JsonProperty("publisher_platforms")
    private List<String> publisherPlatforms;

    @JsonProperty("facebook_positions")
    private List<String> facebookPositions;

    @JsonProperty("instagram_positions")
    private List<String> instagramPositions;

    // ── Creative Type ─────────────────────────────────────────────────────────
    @JsonProperty("creative_type")
    private String creativeType;   // IMAGE (default) | VIDEO | CAROUSEL

    // ── Video Ad ──────────────────────────────────────────────────────────────
    @JsonProperty("video_url")
    private String videoUrl;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    // ── Carousel Ad ───────────────────────────────────────────────────────────
    @JsonProperty("carousel_cards")
    private List<CarouselCardDto> carouselCards;

    // ── Advantage+ Creative (Degrees of Freedom) ──────────────────────────────
    @JsonProperty("degrees_of_freedom")
    private boolean degreesOfFreedom;

    // ── Multi-Image Assets (different ratios for different placements) ─────────
    @JsonProperty("image_assets")
    private List<ImageAssetDto> imageAssets;
}
