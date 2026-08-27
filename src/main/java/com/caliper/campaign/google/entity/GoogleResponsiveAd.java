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
@Table(name = "google_responsive_ad")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GoogleResponsiveAd {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "client_id")
	private String clientId;
	
	@Column(name = "adgroup_id")
	private long adgroupId;
	
	@Column(name = "ad_resource_name")
	private String adResourceName;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "value")
	private String value;
	
	@Column(name = "status")
	private String status;
	
	public static final String HEADLINE = "HEADLINE";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String AD_NAME = "AD_NAME";
	public static final String FINAL_URL = "FINAL_URL";
	public static final String DISPLAY_URL = "DISPLAY_URL";

}
