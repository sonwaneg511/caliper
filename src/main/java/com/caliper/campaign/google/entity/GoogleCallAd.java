package com.caliper.campaign.google.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "google_call_ad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleCallAd {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "client_id")
	private String clientId;
	
	@Column(name = "adgroup_id")
	private long adGroupId;
	
	@Column(name = "ad_resource_name")
	private String adResourceName;
	
	@Column(name = "business_name")
	private String businessName;
	
	@Column(name = "headline_1")
	private String headline1;
	
	@Column(name = "headline_2")
	private String headline2;
	
	@Column(name = "description_1")
	private String description1;
	
	@Column(name = "description_2")
	private String description2;
	
	@Column(name = "phone_number")
	private String phoneNumber;
	
	@Column(name = "verification_url")
	private String verificationUrl;
	
	@Column(name = "final_url")
	private String finalUrl;
	
	@Column(name = "path_1")
	private String path1;
	
	@Column(name = "path_2")
	private String path2;
	
	public static final String COUNTRY_CODE = "IN";
	public static final String AD_TYPE_CALL = "call";
	
}