package com.caliper.campaign.facebook.entity;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meta_adset")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaAdSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "campaign_id")
    private long campaignId;

    @Column(name = "adset_name")
    private String adSetName;

    @Column(name = "meta_adset_id")
    private String metaAdSetId;

    @Column(name = "optimization_goal")
    private String optimizationGoal;

    @Column(name = "billing_event")
    private String billingEvent;

    @Column(name = "bid_amount")
    private Long bidAmount;

    @Column(name = "daily_budget")
    private BigDecimal dailyBudget;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "stop_time")
    private Date stopTime;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;

    @Column(name = "radius")
    private double radius;

    @Column(name = "radius_unit")
    private String radiusUnit;

    @Column(name = "status")
    private String status;

    @Column(name = "targeting_type")
    private String targetingType;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "city")
    private String city;

    @Column(name = "region")
    private String region;

    @Column(name = "bid_strategy")
    private String bidStrategy;

    @Column(name = "gender")
    private String gender;

    @Column(name = "age_min")
    private Long ageMin;

    @Column(name = "age_max")
    private Long ageMax;

    @Column(name = "publisher_platforms")
    private String publisherPlatforms;

    @Column(name = "facebook_positions")
    private String facebookPositions;

    @Column(name = "instagram_positions")
    private String instagramPositions;

    @Column(name = "promoted_object_page_id")
    private String promotedObjectPageId;

    @Column(name = "promoted_object_pixel_id")
    private String promotedObjectPixelId;

    @Column(name = "promoted_object_custom_event_type")
    private String promotedObjectCustomEventType;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    public static final String OPTIMIZATION_GOAL_REACH = "REACH";
    public static final String OPTIMIZATION_GOAL_LINK_CLICKS = "LINK_CLICKS";
    public static final String OPTIMIZATION_GOAL_LANDING_PAGE_VIEWS = "LANDING_PAGE_VIEWS";
    public static final String OPTIMIZATION_GOAL_LEAD_GENERATION = "LEAD_GENERATION";
    public static final String OPTIMIZATION_GOAL_POST_ENGAGEMENT = "POST_ENGAGEMENT";
    public static final String OPTIMIZATION_GOAL_OFFSITE_CONVERSIONS = "OFFSITE_CONVERSIONS";

    public static final String BILLING_EVENT_IMPRESSIONS = "IMPRESSIONS";

    public static final String RADIUS_UNIT_KILOMETER = "kilometer";
    public static final String RADIUS_UNIT_MILE = "mile";

    public static final String TARGETING_TYPE_RADIUS  = "RADIUS";
    public static final String TARGETING_TYPE_PINCODE = "PINCODE";
    public static final String TARGETING_TYPE_CITY    = "CITY";
    public static final String TARGETING_TYPE_REGION  = "REGION";

    public static final String DESTINATION_TYPE_WEBSITE          = "WEBSITE";
    public static final String DESTINATION_TYPE_APP              = "APP";
    public static final String DESTINATION_TYPE_MESSENGER        = "MESSENGER";
    public static final String DESTINATION_TYPE_INSTAGRAM_DIRECT = "INSTAGRAM_DIRECT";
    public static final String DESTINATION_TYPE_INSTANT_FORMS    = "INSTANT_FORMS";
    public static final String DESTINATION_TYPE_PHONE_CALL       = "PHONE_CALL";
    public static final String DESTINATION_TYPE_FACEBOOK_PAGE    = "FACEBOOK_PAGE";
    public static final String DESTINATION_TYPE_WHATSAPP         = "WHATSAPP";

    public static final String BID_STRATEGY_LOWEST_COST          = "LOWEST_COST_WITHOUT_CAP";
    public static final String BID_STRATEGY_BID_CAP              = "LOWEST_COST_WITH_BID_CAP";
    public static final String BID_STRATEGY_COST_CAP             = "COST_CAP";
    public static final String BID_STRATEGY_MIN_ROAS             = "LOWEST_COST_WITH_MIN_ROAS";

    public static final String GENDER_ALL    = "ALL";
    public static final String GENDER_MALE   = "MALE";
    public static final String GENDER_FEMALE = "FEMALE";
}
