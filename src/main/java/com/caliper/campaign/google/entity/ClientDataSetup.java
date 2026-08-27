package com.caliper.campaign.google.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "client_data_setup")
public class ClientDataSetup {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="client_id")
	private String clientId;
	
	@Column(name="industry")
	private String industry;
	
	@Column(name="sub_industry")
	private String subIndustry;
	
	@Column(name="landing_page_url")
	private String landingPageUrl;
	
	@Column(name="youtube_video_url")
	private String youtubeVideoUrl;
	
	@Column(name="phone_number")
	private String phoneNumber;
	
	@Column(name="google_account_id")
	private String googleAccountId;
	
	@Column(name="cpc_bid")
	private double cpcBid;
	
	@Column(name="platform")
	private String platform;
	
	@Column(name="street_address1")
	private String streetAddress1;
	
	@Column(name="street_address2")
	private String streetAddress2;
	
	@Column(name="pincode")
	private String pincode;
	
	@Column(name="ad_phone_number")
	private String adPhoneNumber;
	
	@Column(name="city")
	private String city;
	
	@Column(name="state")
	private String state;
	
	@Column(name="latitude")
	private String latitude;
	
	@Column(name="longitude")
	private String longitude;
	
	@Column(name="radius")
	private double radius;
	
	@Column(name="radius_unit")
	private String radiusUnit;
	
	@Column(name="keyword_source")
	private String keywordSource;
}
