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
@Table(name = "google_campaign_assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleCampaignAssets {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "asset_group_id")
	private long assetGroupId;
	
	@Column(name = "asset_group_asset_resource_name")
	private String assetGroupAssetResourceName;
	
	@Column(name = "asset_resource_name")
	private String assetResourceName;
	
	@Column(name = "campaign_id")
	private long campaignID;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "value")
	private String value;
	
	@Column(name = "image_file_name")
	private String imageFileName;
	
}
