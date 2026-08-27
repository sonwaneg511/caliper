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
@Table(name = "client_account_setup")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientAccountSetup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "client_id")
	private String clientId;

	@Column(name = "industry")
	private String industry;
	
	@Column(name = "sub_industry")
	private String subIndustry;
	
	@Column(name = "youtube_video_url")
	private String youtubeVideoUrl;
	
	@Column(name = "google_account_id")
	private String googleAccountId;
	
	@Column(name = "cpc_bid")
	private double cpcBid;
	
	@Column(name = "platform")
	private String platform;
	
	@Column(name = "keyword_source")
	private String keywordSource;

	@Column(name = "monthly_budget")
	private String monthlyBudget;

	@Column(name = "objective")
	private String objective;

}
