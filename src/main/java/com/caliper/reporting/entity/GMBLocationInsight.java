package com.caliper.reporting.entity;


import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Entity
@Data
@Table(name = "gmb_location_insight")
public class GMBLocationInsight {
	
	public static final String SQL_TABLE = "gmb_location_insight";
	
	public static final String SQL_COLUMN_CLIENT_ID = "client_id";
	public static final String SQL_COLUMN_DEALER_ID = "dealer_id";
	public static final String SQL_COLUMN_METRIC_ALL = "metric_all";
	public static final String SQL_COLUMN_QUERIES_DIRECT = "queries_direct";
	public static final String SQL_COLUMN_QUERIES_INDIRECT = "queries_indirect";
	public static final String SQL_COLUMN_QUERIES_CHAIN = "queries_chain";
	public static final String SQL_COLUMN_VIEWS_MAPS = "views_maps";
	public static final String SQL_COLUMN_VIEWS_SEARCH = "views_search";
	//public static final String SQL_COLUMN_METRIC_TYPE_WEBSITE_CLICKS = "webiste_clicks";
	public static final String SQL_COLUMN_ACTIONS_WEBSITE = "actions_website";
	public static final String SQL_COLUMN_ACTIONS_PHONE = "actions_phone";//-->call_clicks
	//public static final String SQL_COLUMN_BUSINESS_DIRECTION_REQUESTS = "business_direction_requests";
	public static final String SQL_COLUMN_ACTIONS_DRIVING_DIRECTIONS = "actions_driving_directions";
	public static final String SQL_COLUMN_PHOTOS_VIEWS_MERCHANT = "photos_views_merchant";
	public static final String SQL_COLUMN_PHOTOS_VIEWS_CUSTOMERS = "photos_views_customers";
	public static final String SQL_COLUMN_PHOTOS_COUNT_MERCHANT = "photos_count_merchant";
	public static final String SQL_COLUMN_PHOTOS_COUNT_CUSTOMERS = "photos_count_customers";
	public static final String SQL_COLUMN_LOCAL_POST_VIEWS_SEARCH = "local_post_views_search";
	public static final String SQL_COLUMN_LOCAL_POST_ACTIONS_CALL_TO_ACTION = "local_post_actions_call_to_action";
	public static final String SQL_COLUMN_BUSINESS_IMPRESSIONS_DESKTOP_MAPS = "business_impressions_desktop_maps";
	public static final String SQL_COLUMN_BUSINESS_IMPRESSIONS_DESKTOP_SEARCH = "business_impressions_desktop_search";
	public static final String SQL_COLUMN_BUSINESS_IMPRESSIONS_MOBILE_MAPS = "business_impressions_moblie_maps";
	public static final String SQL_COLUMN_BUSINESS_IMPRESSIONS_MOBILE_SEARCH = "business_impressions_moblie_search";
//	public static final String SQL_COLUMN_CALL_CLICKS  = "call_clicks";
	public static final String SQL_COLUMN_REPORT_DATE = "report_date";
	public static final String SQL_COLUMN_YEAR_AND_MONTH = "year_and_month";
	
	public static final String METRIC_TYPE_ALL = "ALL";
	public static final String METRIC_TYPE_DEALER = "DEALER";
	public static final String METRIC_TYPE_METRIC_UNSPECIFIED = "METRIC_UNSPECIFIED";
	public static final String METRIC_TYPE_QUERIES_DIRECT = "QUERIES_DIRECT";
	public static final String METRIC_TYPE_QUERIES_INDIRECT = "QUERIES_INDIRECT";
	public static final String METRIC_TYPE_QUERIES_CHAIN = "QUERIES_CHAIN";
	public static final String METRIC_TYPE_VIEWS_MAPS = "VIEWS_MAPS";
	public static final String METRIC_TYPE_VIEWS_SEARCH = "VIEWS_SEARCH";
	public static final String METRIC_TYPE_ACTIONS_WEBSITE = "ACTIONS_WEBSITE";
	public static final String METRIC_TYPE_ACTIONS_PHONE = "ACTIONS_PHONE";
	public static final String METRIC_TYPE_ACTIONS_DRIVING_DIRECTIONS = "ACTIONS_DRIVING_DIRECTIONS";
	public static final String METRIC_TYPE_PHOTOS_VIEWS_MERCHANT = "PHOTOS_VIEWS_MERCHANT";
	public static final String METRIC_TYPE_PHOTOS_VIEWS_CUSTOMERS = "PHOTOS_VIEWS_CUSTOMERS";
	public static final String METRIC_TYPE_PHOTOS_COUNT_MERCHANT = "PHOTOS_COUNT_MERCHANT";
	public static final String METRIC_TYPE_PHOTOS_COUNT_CUSTOMERS = "PHOTOS_COUNT_CUSTOMERS";
	public static final String METRIC_TYPE_LOCAL_POST_VIEWS_SEARCH= "LOCAL_POST_VIEWS_SEARCH";
	public static final String METRIC_TYPE_LOCAL_POST_ACTIONS_CALL_TO_ACTION = "LOCAL_POST_ACTIONS_CALL_TO_ACTION";
	
	public static final String METRIC_TYPE_BUSINESS_IMPRESSIONS_DESKTOP_MAPS = "BUSINESS_IMPRESSIONS_DESKTOP_MAPS";
	public static final String METRIC_TYPE_BUSINESS_IMPRESSIONS_DESKTOP_SEARCH = "BUSINESS_IMPRESSIONS_DESKTOP_SEARCH";
	public static final String METRIC_TYPE_BUSINESS_IMPRESSIONS_MOBILE_MAPS = "BUSINESS_IMPRESSIONS_MOBILE_MAPS";
	public static final String METRIC_TYPE_BUSINESS_IMPRESSIONS_MOBILE_SEARCH = "BUSINESS_IMPRESSIONS_MOBILE_SEARCH";
	public static final String METRIC_TYPE_BUSINESS_DIRECTION_REQUESTS = "BUSINESS_DIRECTION_REQUESTS";
	public static final String METRIC_TYPE_CALL_CLICKS = "CALL_CLICKS";
	public static final String METRIC_TYPE_WEBSITE_CLICKS = "WEBSITE_CLICKS";
	
	public static final String METRIC_OPTION_AGGREGATED_TOTAL = "AGGREGATED_TOTAL";
	public static final String METRIC_VALUE = "value";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@Column(name = "client_id")
	private String clientId;
	
	@Column(name = "dealer_id")
	private String dealerId;
	
	@Column(name = "metric_all")
	private long metricAll;
	
	@Column(name = "queries_direct")
	private long queriesDirect;
	
	@Column(name = "queries_indirect")
	private long queriesIndirect;
	
	@Column(name = "queries_chain")
	private long queriesChain;
	
	@Column(name = "views_maps")
	private long viewsMaps;
	
	@Column(name = "views_search")
	private long viewsSearch;
	
	@Column(name = "actions_website")
	private long actionsWebsite;
	
	@Column(name = "actions_phone")
	private long actionsPhone;
	
	@Column(name = "actions_driving_directions")
	private long actionsDrivingDirections;
	
	@Column(name = "photos_views_merchant")
	private long photosViewsMerchant;
	
	@Column(name = "photos_views_customers")
	private long photosViewsCustomers;
	
	@Column(name = "photos_count_merchant")
	private long photosCountMerchant;
	
	@Column(name = "photos_count_customers")
	private long photosCountCustomers;
	
	@Column(name = "local_post_views_search")
	private long localPostViewsSearch;
	
	@Column(name = "local_post_actions_call_to_action")
	private long localPostActionsCallToAction;
	
	@Column(name = "business_impressions_desktop_maps")
	private long businessImpressionsDesktopMaps;
	
	@Column(name = "business_impressions_desktop_search")
	private long businessImpressionsDesktopSearch;
	
	@Column(name = "business_impressions_moblie_maps")
	private long businessImpressionsMoblieMaps;
	
	@Column(name = "business_impressions_moblie_search")
	private long businessImpressionsMoblieSearch;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "report_date")
	private Date reportDate;
	
}
