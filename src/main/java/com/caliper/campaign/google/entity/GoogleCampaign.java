package com.caliper.campaign.google.entity;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "google_campaign")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleCampaign {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "client_id")
	private String clientId;
	
	@Column(name = "campaign_name")
	private String campaignName;
	
	@Column(name = "campaign_resource_name")
	private String campaignResourceName;
	
	@Column(name = "platform")
	private String platform;
	
	@Column(name = "google_account_id")
	private String googleAccountID;
	
	@Column(name = "dealer_id")
	private String dealerId;
	
	@Column(name = "daily_budget")
	private BigDecimal dailyBudget;
	
	@Column(name = "total_budget")
	private BigDecimal totalBudget;
	
	@Column(name = "budget_resource_name")
	private String budgetResourceName;
	
	@Column(name = "campaign_structure_type")
	private String campaignStructureType;
	
	@Column(name = "campaign_structure_sub_type")
	private String campaignStructureSubType;
	
	@Column(name = "model")
	private String model;
	
	@Column(name = "is_portfolio_bidding_strategy")
	private boolean isPortfolioBiddingStrategy;
	
	@Column(name = "bidding_strategy")
	private String biddingStrategy;
	
	@Column(name = "bidding_value")
	private String biddingValue;
	
	@Column(name = "is_target_google_search")
	private boolean isTargetGoogleSearch;
	
	@Column(name = "is_target_search_network")
	private boolean isTargetSearchNetwork;
	
	@Column(name = "is_target_content_network")
	private boolean isTargetContentNetwork;
	
	@Column(name = "is_target_partner_search_network")
	private boolean isTargetPartnerSearchNetwork;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "start_date")
	private Date startDate;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "end_date")
	private Date endDate;
	
	@Column(name = "last_modified_date")
	private Date lastModidfiedDate;
	
	@Column(name = "last_modified_by")
	private String lastModidfiedBy;
	
	@Column(name = "is_ad_scheduled")
	private boolean isAdScheduled;
	
	@Column(name = "call_extension")
	private boolean callExtension;
	
	@Column(name = "call_asset_resource_name")
	private String callAssetResourceName;
	
	@Column(name = "call_association_resource_name")
	private String callAssociationResourceName;
	
	@Column(name = "location_info_resource_name")
	private String locationInfoResourceName;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "comment")
	private String comment;
	
	@Column(name = "client_comment")
	private String clientComment;
	
	@Column(name = "client_objective")
	private String clientObjective;
	
	@Column(name = "is_radius_target")
	private boolean isRadiusTarget;
	
	@Column(name = "is_pincode_target")
	private boolean isPincodeTarget;

	@Column(name = "location_id")
	private Long locationId;

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

	public static final String SEARCH_CAMPAIGN = "Search campaign";
	public static final String CALL_AD_CAMPAIGN = "Call ad campaign";
	public static final String DEMAND_GEN_MULTI_ASSET_CAMPAIGN = "Demand gen multi asset campaign";
	public static final String DEMAND_GEN_VIDEO_ASSET_CAMPAIGN = "Demand gen video asset campaign";
	public static final String PMAX_CAMPAIGN = "Pmax campaign";
	public static final String CALIPER_CAMPAIGN_STATUS_PAYMENT_PENDING = "Payment Pending";
	public static final String CALIPER_CAMPAIGN_STATUS_PAYMENT_SUCCESSFUL = "Payment Processed, Pending Deployment";
	public static final String CALIPER_CAMPAIGN_STATUS_PAYMENT_FAILED = "Payment Failed";
	public static final String CALIPER_CAMPAIGN_STATUS_PENDING_DEPLOYMENT = "Pending Deployment";
	public static final String CALIPER_CAMPAIGN_STATUS_PAYMENT_PROCESSING = "Payment Processing";
	public static final String CALIPER_CAMPAIGN_STATUS_DEPLOYED = "Deployed";
	
	public static final String KILOMETERS = "KILOMETERS";
	public static final String MILES = "MILES";
	
	public static final String CALIPER_CAMPAIGN_STATUS_LIVE = "Live";
	
	public static final String CALIPER_CAMPAIGN_STATUS_PAUSED = "Paused";
	public static final String CLIENT = "client";

	// Derived purely from startDate/endDate vs today — independent of the manually-triggered
	// Live/Paused/deployment status above, which never automatically reconciles with dates.
	public static final String SCHEDULE_STATUS_SCHEDULED = "Scheduled";
	public static final String SCHEDULE_STATUS_ACTIVE = "Active";
	public static final String SCHEDULE_STATUS_COMPLETED = "Completed";
	
	public static final String ROLE_CLIENT = "client";
	public static final String ROLE_HUB_USER = "hub_user";
}