package com.caliper.campaign.google.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "google_campaign_geo_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCampaignGeoDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "campaign_id")
	private long campaignId;
	
	@Column(name = "targeting_type")
	private String targetingType;
	
	@Column(name = "geo_targeting_id")
	private long geoTargetingId;
	
	@Column(name = "targeting_display_name")
	private String targetingDisplayName;
	
	@Column(name = "targeting_resource_name")
	private String targetingResourceName;
	
	public static final String CITY = "city";
	public static final String STATE = "state";
	public static final String PINCODE = "pincode";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String RADIUS = "radius";
	public static final String RADIUS_UNIT = "radius_unit";
}
