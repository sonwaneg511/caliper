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
@Table(name = "google_adgroup_target_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAdgroupTargetDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "client_id")
	private long clientId;
	@Column(name = "ad_group_id")
	private long adGroupId;
	@Column(name = "targeting_type")
	private String targetingType;
	@Column(name = "geo_targeting_id")
	private long geoTargetingId;
	@Column(name = "targeting_display_name")
	private String targetingDisplayName;
	@Column(name = "asset_resource_name")
	private String assetResourceName;
	
}
