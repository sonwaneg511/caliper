package com.caliper.campaign.google.repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.QueryJobConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.caliper.bigquery.service.BigQueryConfig;
import com.caliper.campaign.google.entity.ArtAdwordsData;

@Repository
public class ArtAdwordsDataBQRepository {

	@Autowired
	private BigQueryConfig bigQueryConfig;


	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static final String PROJECT_ID = "reporting-automation-123456";
	private static final String CLIENT_NAME = "myNewDataSet";

	private String getSafeString(FieldValue field) {
		return (field == null || field.isNull()) ? "" : field.getStringValue();
	}

	private long getSafeLong(FieldValue field) {
		return (field == null || field.isNull()) ? 0L : field.getLongValue();
	}
	private double getSafeDouble(FieldValue field) {
		return (field == null || field.isNull()) ? 0.0 : field.getDoubleValue();
	}


	public static String convertToUTC(String epochSecondsStr) {
		// Use BigDecimal to avoid floating-point precision issues
		BigDecimal bd = new BigDecimal(epochSecondsStr);

		// Separate integer seconds and fractional remainder
		BigDecimal[] parts = bd.divideAndRemainder(BigDecimal.ONE);
		long seconds = parts[0].longValueExact();

		// fractional part -> nanoseconds (scale to 9 digits)
		int nanos = parts[1].movePointRight(9).intValue(); // e.g. .123 -> 123000000

		Instant instant = Instant.ofEpochSecond(seconds, nanos);

		// Formatter that prints exactly 6 fractional digits (microseconds)
		DateTimeFormatter fmt = new DateTimeFormatterBuilder()
				.appendPattern("yyyy-MM-dd HH:mm:ss")
				.appendLiteral('.')
				.appendFraction(ChronoField.NANO_OF_SECOND, 6, 6, true) // exactly 6 digits
				.appendLiteral(" UTC")
				.toFormatter()
				.withZone(ZoneOffset.UTC);

		return fmt.format(instant);
	}

	public Double sumDeliveredImpressionsByCampaignResourceName(List<String>campaignResourceNames) throws FileNotFoundException, IOException, JobException, InterruptedException {
		
		BigQuery bigQuery = bigQueryConfig.getBigQueryDao(PROJECT_ID);

		String inClause = campaignResourceNames.stream()
		        .map(name -> "'" + name + "'")
		        .collect(Collectors.joining(","));
		
		String query = "SELECT Impressions FROM `"
		        + PROJECT_ID + "." + CLIENT_NAME + "." + ArtAdwordsData.SQL_TABLE + "` "
		        + "WHERE CampaignId IN (" + inClause + ")";
		
		System.out.println("query :: "+query);
		QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
		List<ArtAdwordsData> impressionsList = new ArrayList<ArtAdwordsData>();

		System.out.println("Table rows:");
		for (FieldValueList row : bigQuery.query(queryConfig).iterateAll()) {
			System.out.println(row);
			double impressions   = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_IMPRESSIONS));

			ArtAdwordsData adwordsData = ArtAdwordsData.builder()
			        .impressions(impressions)
			        .build();
			
			impressionsList.add(adwordsData);
		}	
		
		double totalImpressions = 0.0;

		for (ArtAdwordsData data : impressionsList) {
		    totalImpressions += data.getImpressions();
		}

		return totalImpressions;
		
	}
	

	public List<ArtAdwordsData> getArtAdwordsData(long customerId, Date segmentsStartDate, Date segmentsEndDate) throws FileNotFoundException, JobException, InterruptedException, ParseException, IOException {
		System.out.println("IN BQ DAO");
		BigQuery bigQuery = bigQueryConfig.getBigQueryDao(PROJECT_ID);


		String campaignStartDate = sdf.format(segmentsStartDate);
		String campaignEndDate = sdf.format(segmentsEndDate);

		String query = "select * from `"+PROJECT_ID+"."+CLIENT_NAME+"."+ArtAdwordsData.SQL_TABLE+"` where UniqueId = '"+customerId+"' and date >= '"+campaignStartDate+"' and date <= '"+campaignEndDate+"'";
		System.out.println("query :: "+query);
		QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
		//bigQuery.query(queryConfig);

		List<ArtAdwordsData> artAdwordsDataList = new ArrayList<ArtAdwordsData>();

		System.out.println("Table rows:");
		for (FieldValueList row : bigQuery.query(queryConfig).iterateAll()) {
			System.out.println(row);

			String createdAtDate = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_CREATED_AT));
			System.out.println("createdAtDate :: " + createdAtDate);

			String resultCreatedAtDate = convertToUTC(createdAtDate);
			Date createdAt = sdf.parse(resultCreatedAtDate);

			String reportType = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_REPORT_TYPE));
			String adGroupName = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_AD_GROUP_NAME));
			String adGroupId   = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_AD_GROUP_ID));
			String adGroupStatus = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_AD_GROUP_STATUS));
			String campaignId  = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_CAMPAIGN_ID));
			String campaignName= getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_CAMPAIGN_NAME));
			String apiDate     = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_DATE));
			Date date          = sdf.parse(apiDate);
			double clicks        = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_CLICKS));
			double impressions   = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_IMPRESSIONS));
			double ctr           = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_CTR));
			double cost          = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_COST));
			double conversions   = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_CONVERSIONS));
			double viewThruConversions         = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_VIEW_THRU_CONVERSIONS));
			double costPerConversion           = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_COST_PER_CONVERSION));
			double conversionRate= getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_CONVERSION_RATE));
			String accountCurrencyCode       = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_ACCOUNT_CURRENCY_CODE));
			String biddingStrategyName       = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_BIDDING_STRATEGY_NAME));
			String biddingStrategyType       = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_BIDDING_STRATEGY_TYPE));
			String advertisingChannelType    = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_ADVERTISING_CHANNEL_TYPE));
			String advertisingChannelSubType = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_ADVERTISING_CHANNEL_SUB_TYPE));
			String campaignStatus= getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_CAMPAIGN_STATUS));
			double amount        = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_AMOUNT));
			double searchImpressionShare       = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_SEARCH_IMPRESSION_SHARE));
			double averageCpm    = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_AVERAGE_CPM));
			double videoViews    = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_VIDEO_VIEWS));
			double videoViewRate = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_VIDEO_VIEW_RATE));
			double videoQuartile25Rate         = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_VIDEO_QUARTILE_25_RATE));
			double videoQuartile50Rate         = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_VIDEO_QUARTILE_50_RATE));
			double videoQuartile75Rate         = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_VIDEO_QUARTILE_75_RATE));
			double videoQuartile100Rate        = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_VIDEO_QUARTILE_100_RATE));
			double interactions  = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_INTERACTIONS));
			double interactionRate = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_INTERACTION_RATE));
			String city        = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_CITY));
			String region      = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_REGION));
			String country     = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_COUNTRY));
			double iACampaignId  = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_IA_CAMPAIGN_ID));
			String uniqueId    = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_UNIQUE_ID));
			double engagementRate= getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_ENGAGEMENT_RATE));
			String adGroupType = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_AD_GROUP_TYPE));
			double averageCpc    = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_AVERAGE_CPC));
			double phoneCalls    = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_PHONE_CALLS));
			double phoneImpressions= getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_PHONE_IMPRESSION));
			double conversionsFromInteractionsRate = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_CONVERSIONS_FROM_INTERACTIONS_RATE));
			double searchRankLostImpressionShare   = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_SEARCH_RANK_LOST_IMPRESSION_SHARE));
			String campaignGroupName         = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_CAMPAIGN_GROUP_NAME));
			double allConversionsFromStoreVisit = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_ALL_CONVERSIONS_FROM_STORE_VISIT));
			double searchBudgetLostImpressionShare = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_SEARCH_BUDGET_LOST_IMPRESSION_SHARE));
			double allConversions= getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_ALL_CONVERSIONS));
			double engagements   = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_ENGAGEMENTS));
			double searchAbsTopIS= getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_SEARCH_ABS_TOP_IS));
			double searchExactMatchIS          = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_SEARCH_EXACT_MATCH_IS));
			double searchBudgetLostAbsoluteTopImpressionShare =
					getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_SEARCH_BUDGET_LOST_ABSOLUTE_TOP_IMPRESSION_SHARE));
			double searchRankLostAbsoluteTopImpressionShare =
					getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_SEARCH_RANK_LOST_ABSOLUTE_TOP_IMPRESSION_SHARE));
			double campaignBudgetAmountMicros  = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_CAMPAIGN_BUGET_AMOUNT_MICROS));
			double searchBudgetLostTopImpressionShare = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_SEARCH_BUDGET_LOST_TOP_IMPRESSION_SHARE));
			double searchRankLostTopImpressionShare =
					getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_SEARCH_RANK_LOST_TOP_IMPRESSION_SHARE));
			double viewThroughConversions      = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_VIEW_THROUGH_CONVERSIONS));
			double crossDeviceConversions      = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_CROSS_DEVICE_CONVERSIONS));
			double targetCpaMicros = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_TARGET_CPA_MICROS));
			double targetImpressionShareCpcBidCeilingMicros =
					getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_TARGET_IMPRESSION_SHARE_CPC_BID_CEILING_MICROS));
			double targetRoas    = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_TARGET_ROAS));
			double phoneThroughRate= getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_PHONE_THROUGH_RATE));
			double gmailSecondaryClicks        = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_GMAIL_SECONDARY_CLICKS));
			double gmailForwards = getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_GMAIL_FORWARDS));
			double searchClickShare= getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_SEARCH_CLICK_SHARE));
			double conversionsValue= getSafeDouble(row.get(ArtAdwordsData.SQL_COLUMN_CONVERSION_VALUE));
			String partnerId   = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_PARTNER_ID));
			String partnerName = getSafeString(row.get(ArtAdwordsData.SQL_COLUMN_PARTNER_NAME));


			ArtAdwordsData artAdwordsData = new ArtAdwordsData(createdAt, reportType, adGroupName, adGroupId, adGroupStatus, campaignId, campaignName, date, clicks, impressions, ctr, cost, conversions, viewThruConversions, costPerConversion, conversionRate, accountCurrencyCode, biddingStrategyName, biddingStrategyType, advertisingChannelType, advertisingChannelSubType, campaignStatus, amount, searchImpressionShare, averageCpm, videoViews, videoViewRate, videoQuartile25Rate, videoQuartile50Rate, videoQuartile75Rate, videoQuartile100Rate, interactions, interactionRate, city, region, country, iACampaignId, uniqueId, engagementRate, adGroupType, averageCpc, phoneCalls, phoneImpressions, conversionsFromInteractionsRate, searchRankLostImpressionShare, campaignGroupName, allConversionsFromStoreVisit, searchBudgetLostImpressionShare, allConversions, engagements, searchAbsTopIS, searchExactMatchIS, searchBudgetLostAbsoluteTopImpressionShare, searchRankLostAbsoluteTopImpressionShare, campaignBudgetAmountMicros, searchBudgetLostTopImpressionShare, searchRankLostTopImpressionShare, viewThroughConversions, crossDeviceConversions, targetCpaMicros, targetImpressionShareCpcBidCeilingMicros, targetRoas, phoneThroughRate, gmailSecondaryClicks, gmailForwards, searchClickShare, conversionsValue, partnerId, partnerName);
			artAdwordsDataList.add(artAdwordsData);
		}
		return artAdwordsDataList;
	}





}
