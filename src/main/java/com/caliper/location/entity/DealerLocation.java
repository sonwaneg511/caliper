package com.caliper.location.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dealer_location")
public class DealerLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonIgnore
    private Long id;

    @Column(name = "client_id")
    @JsonProperty("client_id")
    private String clientId;

    @Column(name = "name_with_location_desc")
    @JsonProperty("name_with_location_desc")
    private String nameWithLocationDesc;

    @Column(name = "dealer_id")
    @JsonProperty("dealer_id")
    private String dealerId;

    @Column(name = "gmb_location_id")
    @JsonProperty("gmb_location_id")
    private String gmbLocationId;

    @Column(name = "dealer_name")
    @JsonProperty("dealer_name")
    private String dealerName;

    @Column(name = "type")
    @JsonProperty("type")
    private String type;

    @Column(name = "address")
    @JsonProperty("address")
    private String address;

    @Column(name = "address1")
    @JsonProperty("address1")
    private String address1;

    @Column(name = "address2")
    @JsonProperty("address2")
    private String address2;

    @Column(name = "address3")
    @JsonProperty("address3")
    private String address3;

    @Column(name = "area")
    @JsonProperty("area")
    private String area;

    @Column(name = "circle")
    @JsonProperty("circle")
    private String circle;

    @Column(name = "city")
    @JsonProperty("city")
    private String city;

    @Column(name = "state")
    @JsonProperty("state")
    private String state;

    @Column(name = "country")
    @JsonProperty("country")
    private String country;

    @Column(name = "country_code")
    @JsonProperty("country_code")
    private String countryCode;

    @Column(name = "pincode")
    @JsonProperty("pincode")
    private String pincode;

    @Column(name = "latitude")
    @JsonProperty("latitude")
    private String latitude;

    @Column(name = "longitude")
    @JsonProperty("longitude")
    private String longitude;

    @Column(name = "gmb_phone_number")
    @JsonProperty("gmb_phone_number")
    private String gmbPhoneNumber;

    @Column(name = "microsite_phone_number")
    @JsonProperty("microsite_phone_number")
    private String micrositePhoneNumber;

    @Column(name = "meta_ctwa_phone_number")
    @JsonProperty("meta_ctwa_phone_number")
    private String metaCtwaPhoneNumber;

    @Column(name = "adwords_phone_number")
    @JsonProperty("adwords_phone_number")
    private String adwordsPhoneNumber;

    @Column(name = "adwords_ctwa_phone_number")
    @JsonProperty("adwords_ctwa_phone_number")
    private String adwordsCtwaPhoneNumber;

    @Column(name = "meta_phone_number")
    @JsonProperty("meta_phone_number")
    private String metaPhoneNumber;

    @Column(name = "email")
    @JsonProperty("email")
    private String email;

    @Column(name = "description", columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @Column(name = "primary_category_gmb")
    @JsonProperty("primary_category_gmb")
    private Long primaryCategoryGMB;

    @Column(name = "additional_categories_gmb")
    @JsonProperty("additional_categories_gmb")
    private String additionalCategoriesGMB;

    @Column(name = "primary_category_facebook")
    @JsonProperty("primary_category_facebook")
    private Long primaryCategoryFacebook;

    @Column(name = "additional_categories_facebook")
    @JsonProperty("additional_categories_facebook")
    private String additionalCategoriesFacebook;

    @Column(name = "additional_phones")
    @JsonProperty("additional_phones")
    private String additionalPhones;

    @Column(name = "status")
    @JsonProperty("status")
    private String status;

    @Column(name = "appointment_link")
    @JsonProperty("appointment_link")
    private String appointmentLink;

    @Column(name = "store_page_url")
    @JsonProperty("store_page_url")
    private String storePageUrl;

    @Column(name = "language_code")
    @JsonProperty("language_code")
    private String languageCode;

    @Column(name = "gmb_enabled")
    @JsonProperty("gmb_enabled")
    private Boolean gmbEnabled;

    @Column(name = "facebook_enabled")
    @JsonProperty("facebook_enabled")
    private Boolean facebookEnabled;

    @Column(name = "store_page_enabled")
    @JsonProperty("store_page_enabled")
    private Boolean storePageEnabled;

    @Column(name = "facebook_page_id")
    @JsonProperty("facebook_page_id")
    private String facebookPageId;

    @Column(name = "gmb_title")
    @JsonProperty("gmb_title")
    private String gmbTitle;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "dealer_operation_hours_id")
    @JsonProperty("dealer_operation_hours")
    private DealerOperationHours dealerOperationHours;

    @Column(name = "meta_region_page")
    @JsonProperty("meta_region_page")
    private String metaRegionPage;

    @Column(name = "qr_created")
    @JsonProperty("qr_created")
    private Boolean qrCreated;

    @Column(name = "qr_image")
    @JsonProperty("qr_image")
    private String qrImage;

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

    @Column(name = "open_info_can_reopen")
    @JsonProperty("open_info_can_reopen")
    private Boolean openInfoCanReopen;

    @Column(name = "open_info_status")
    @JsonProperty("open_info_status")
    private String openInfoStatus;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "inserted_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @JsonProperty("inserted_date")
    private Date insertedDate;

    public static final String GMB = "GMB";
    public static final String FACEBOOK = "FACEBOOK";
}
