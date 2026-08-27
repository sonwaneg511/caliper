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
@Table(name = "client_location_setup")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientLocationSetup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "client_id")
	private String clientId;
	
	@Column(name = "dealer_id")
	private String dealerId;
	
	@Column(name = "latitude")
	private String latitude;
	
	@Column(name = "longitude")
	private String longitude;
	
	@Column(name = "radius")
	private double radius;
	
	@Column(name = "radius_unit")
	private String radiusUnit;
	
	@Column(name = "ad_phone_number")
	private String adPhoneNumber;
	
	@Column(name = "landing_page_url")
	private String landingPageUrl;
	
	@Column(name = "client_campaign_phone_number")
	private String  clientCampaignPhoneNumber;

	public ClientLocationSetup(String clientId, String dealerId, String latitude, String longitude, double radius,
			String radiusUnit, String adPhoneNumber, String landingPageUrl, String clientCampaignPhoneNumber) {
		super();
		this.clientId = clientId;
		this.dealerId = dealerId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
		this.radiusUnit = radiusUnit;
		this.adPhoneNumber = adPhoneNumber;
		this.landingPageUrl = landingPageUrl;
		this.clientCampaignPhoneNumber = clientCampaignPhoneNumber;
	}
	
}
