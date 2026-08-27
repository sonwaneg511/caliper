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
@Table(name = "google_campaign_asset_group")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleCampaignAssetGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "campaign_id")
	private long campaignId;
	
	@Column(name = "asset_group_name")
	private String assetGroupName;
	
	@Column(name = "asset_group_resource_name")
	private String assetGroupResourceName;
	
	@Column(name = "final_url")
	private String finalUrl;
	
	@Column(name = "mobile_url")
	private String mobileUrl;
	
}
