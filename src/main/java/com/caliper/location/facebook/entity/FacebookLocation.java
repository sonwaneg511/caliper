package com.caliper.location.facebook.entity;

import java.util.Date;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Data
@Table(name = "facebook_location")
public class FacebookLocation {

    @Id
    @Column(name = "facebook_page_id")
    private String facebookPageId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "parent_page_id")
    private String parentPageId;

    @Column(name = "dealer_id")
    private String dealerId;

    @Column(name = "name")
    private String name;

    @Column(name = "name_with_location_desc")
    private String nameWithLocationDesc;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "facebook_page_url")
    private String facebookPageUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private Long category;

    @Column(name = "sub_category")
    private String subCategory;

    @Column(name = "labels")
    private String labels;

    @Column(name = "is_published")
    private boolean isPublished;

    @Column(name = "access_token")
    private String accessToken;

    @Temporal(TemporalType.DATE)
    @Column(name = "access_token_expiry")
    private Date accessTokenExpiry;

    @Column(name = "follower_count")
    private String followerCount;

    @Temporal(TemporalType.DATE)
    @Column(name = "last_inserted")
    private Date lastInserted;

    @Column(name = "store_number")
    private String storeNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "facebook_operation_hours_id")   // FK column
    private FacebookOperationHours facebookOperationHours;
    
    public static final String LOCATION_STATE_DUPLICATE = "duplicate";
	public static final String LOCATION_SOURCE_FACEBOOK = "Facebook";
}
