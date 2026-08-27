package com.caliper.campaign.google.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "demand_gen_asset_ad")
@Setter
public class DemandGenAssetAd {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="client_id")
	private String clientId;
	
	@Column(name="adgroup_id")
	private long adgroupId;
	
	@Column(name="ad_resource_name")
	private String adResourceName;
	
	@Column(name="type")
	private String type;
	
	@Column(name="value")
	private String value;
	
	@Column(name="status")
	private String status;
	
	@Column(name="ad_type")
	private String adType;
}
