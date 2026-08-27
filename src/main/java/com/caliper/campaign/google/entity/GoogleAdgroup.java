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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "google_adgroup")
@Data
@Builder
public class GoogleAdgroup {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="ad_group_name")
	private String adGroupName;
	
	@Column(name="campaign_id")
	private long campaignId;
	
	@Column(name="ad_group_resource_name")
	private String adGroupResourceName;
	
	@Column(name="cpc_bid")
	private String cpcBid;
}
