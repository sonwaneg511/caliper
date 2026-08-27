package com.caliper.location.gmb.entity;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "gmb_location")
public class GMBLocation {

	public static final String SQL_TABLE = "gmb_review";

	
    @Id
    @Column(name = "gmb_location_id")
    @JsonProperty("gmb_location_id")
    private String gmbLocationId;

    @Column(name = "account_id")
    @JsonProperty("account_id")
    private String accountId;

    @Column(name = "client_id")
    @JsonProperty("client_id")
    private String clientId;

    @Column(name = "dealer_id")
    @JsonProperty("dealer_id")
    private String dealerId;

    @Column(name = "name")
    @JsonProperty("name")
    private String name;

    @Column(name = "address")
    @JsonProperty("address")
    private String address;

    @Column(name = "area")
    @JsonProperty("area")
    private String area;

    @Column(name = "city")
    @JsonProperty("city")
    private String city;

    @Column(name = "state")
    @JsonProperty("state")
    private String state;

    @Column(name = "pincode")
    @JsonProperty("pincode")
    private Long pincode;

    @Column(name = "latitude")
    @JsonProperty("latitude")
    private String latitude;

    @Column(name = "longitude")
    @JsonProperty("longitude")
    private String longitude;

    @Column(name = "country_code")
    @JsonProperty("country_code")
    private String countryCode;

    @Column(name = "language_code")
    @JsonProperty("language_code")
    private String languageCode;

    @Column(name = "phone_number")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @Column(name = "additional_phones")
    @JsonProperty("additional_phones")
    private String additionalPhones;

    @Column(name = "map_url")
    @JsonProperty("map_url")
    private String mapUrl;

    @Column(name = "new_review_url")
    @JsonProperty("new_review_url")
    private String newReviewUrl;

    @Column(name = "open_info_can_reopen")
    @JsonProperty("open_info_can_reopen")
    private Boolean openInfoCanReopen;

    @Column(name = "open_info_status")
    @JsonProperty("open_info_status")
    private String openInfoStatus;

    @Column(name = "website_url")
    @JsonProperty("website_url")
    private String websiteUrl;

    @Column(name = "description", length = 2000) 
    @JsonProperty("description")
    private String description;

    @Column(name = "primary_category")
    @JsonProperty("primary_category")
    private Long primaryCategory;

    @Column(name = "additional_categories")
    @JsonProperty("additional_categories")
    private String additionalCategories;

    @Column(name = "labels")
    @JsonProperty("labels")
    private String labels;

    @Column(name = "status")
    @JsonProperty("status")
    private String status;

    @Column(name = "place_action_id")
    @JsonProperty("place_action_id")
    private String placeActionId;

    @Column(name = "appointment_link")
    @JsonProperty("appointment_link")
    private String appointmentLink;

    @Column(name = "address1")
    @JsonProperty("address1")
    private String address1;

    @Column(name = "address2")
    @JsonProperty("address2")
    private String address2;

    @Column(name = "address3")
    @JsonProperty("address3")
    private String address3;

    @Temporal(TemporalType.DATE)
    @Column(name = "inserted_date")
    @JsonProperty("inserted_date")
    private Date insertedDate;

    @Column(name = "whatsapp_url")
    @JsonProperty("whatsapp_url")
    private String whatsappUrl;

    @Column(name = "instagram_url")
    @JsonProperty("instagram_url")
    private String instagramUrl;

    @Column(name = "youtube_url")
    @JsonProperty("youtube_url")
    private String youtubeUrl;

    @Column(name = "facebook_url")
    @JsonProperty("facebook_url")
    private String facebookUrl;

    @Column(name = "twitter_url")
    @JsonProperty("twitter_url")
    private String twitterUrl;

    @Column(name = "linkedin_url")
    @JsonProperty("linkedin_url")
    private String linkedinUrl;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "gmb_operation_hours_id") // FK column
    @JsonProperty("gmb_operation_hours_id")
    private GMBOperationHours gmbOperationHours;

    // Static constants remain unchanged
    public static final long DEFAULT_ID = 0;
    public static final String LOCATION_OPEN_STATUS = "OPEN";
    public static final String LOCATION_VERIFICATION_STATUS = "VERIFIED";
    public static final String LOCATION_STATE_UNSPECIFIED = "RECOMMENDATION_REASON_UNSPECIFIED";
    public static final String LOCATION_STATE_SUSPENDED = "BUSINESS_LOCATION_SUSPENDED";
    public static final String LOCATION_STATE_DISABLED = "BUSINESS_LOCATION_DISABLED";
    public static final String LOCATION_STATE_DUPLICATE = "duplicate";
    public static final String LOCATION_STATE_DISCONNECTED = "disconnected";
    public static final String LOCATION_STATE_PENDINGREVIEW = "pendingReview";
    public static final String LOCATION_STATE_VERIFIED = "verified";
    public static final String LOCATION_STATE_UNVERIFIED = "unverified";
    public static final String LOCATION_STATE_OTHER = "other";
    public static final String LOCATION_STATE_VOICE_OF_MERCHANT = "hasVoiceOfMerchant";
    public static final String LOCATION_STATE_BUSINESS_AUTHORITY = "hasBusinessAuthority";
    public static final String LOCATION_STATE_CLOSED_TEMPORARILY = "CLOSED_TEMPORARILY";
    public static final String LOCATION_STATE_CLOSED_PERMANENTLY = "CLOSED_PERMANENTLY";
    public static final String LOCATION_STATE_BUSINESS_LOCATION_SUSPENDED = "BUSINESS_LOCATION_SUSPENDED";
    public static final String LOCATION_STATE_BUSINESS_LOCATION_DISABLED = "BUSINESS_LOCATION_DISABLED";
    public static final String LOCATION_SOURCE_GMB = "GMB";

	
}
