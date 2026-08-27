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
@Table(name = "google_site_link")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleSiteLink {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "campaign_id")
	private long campaignId;

	@Column(name = "headline")
	private String headline;
	
	@Column(name = "description1")
	private String description1;
	
	@Column(name = "description2")
	private String description2;
	
	@Column(name = "final_url")
	private String finalUrl;
	
	@Column(name = "mobile_url")
	private String mobileUrl;
	
	@Column(name = "resource_name")
	private String resourceName;
	
}
