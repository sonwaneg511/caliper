package com.caliper.location.bigQuery;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.caliper.reporting.entity.GMBLocationInsight;
import com.caliper.test.entity.GMBReviewTest;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllRequest.Builder;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableId;


public class GMBLocationInsightBQRepository	 {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static final String PROJECT_ID = "ia-reports-123456";
//	private List<GMBLocationInsight> gmbLocationInsightList = null;

	public void insert(BigQuery bigquery, String clientName, String clientId, String dealerId, long metricAll, long queriesDirect,
			long queriesIndirect, long queriesChain, long viewsMaps, long viewsSearch, long actionsWebsite,
			long actionsPhone, long actionsDrivingDirections, long photosViewsMerchant, long photosViewsCustomers,
			long photosCountMerchant, long photosCountCustomers, long localPostViewsSearch, long localPostActionsCallToAction,
			long businessImpressionsDesktopMaps,
			long businessImpressionsDesktopSearch, long businessImpressionsMoblieMaps,
			long businessImpressionsMoblieSearch, 
			Date reportDate) {
		
		String reportDateStr = sdf.format(reportDate);

		String tableName = GMBLocationInsight.SQL_TABLE;
		TableId tableId = TableId.of(clientName, tableName); // TODO get table name from domain class' table name
		InsertAllResponse response =
				bigquery.insertAll(
						InsertAllRequest.newBuilder(tableId)
						// More rows can be added in the same RPC by invoking .addRow() on the builder.
						// You can also supply optional unique row keys to support de-duplication
						// scenarios.
						.addRow(mapRow(clientId, dealerId, metricAll, queriesDirect, queriesIndirect, queriesChain, viewsMaps, viewsSearch,
								actionsWebsite, actionsPhone, actionsDrivingDirections, photosViewsMerchant, photosViewsCustomers, photosCountMerchant, photosCountCustomers, 
								localPostViewsSearch, localPostActionsCallToAction, businessImpressionsDesktopMaps, businessImpressionsDesktopSearch,
								businessImpressionsMoblieMaps, businessImpressionsMoblieSearch, reportDateStr))
						.build());
		if (response.hasErrors()) { 
			String error = "";
			for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
				error += entry.getValue() + " | ";
			}
			error = error.trim();
			throw new RuntimeException(error);
		}
	}
	
	public void insert(BigQuery bigquery, String clientName, List<GMBLocationInsight> data) {
		String tableName = GMBLocationInsight.SQL_TABLE;
		
		TableId tableId = TableId.of(clientName, tableName);
		long count = 0;
		List<GMBLocationInsight> batch = new ArrayList<GMBLocationInsight>();
		for (GMBLocationInsight d : data) {
			batch.add(d);
			count++;
			if ((count % 100) == 0) {
				executeBatch(bigquery, tableId, batch);
				batch.clear();
			}
		}
		if (batch.size() > 0) {
			executeBatch(bigquery, tableId, batch);
		}
	}
	
	private InsertAllResponse executeBatch(BigQuery bigquery, TableId tableId, List<GMBLocationInsight> batch) {
		Builder builder = InsertAllRequest.newBuilder(tableId);
		for (GMBLocationInsight data : batch) {
			String reportDateStr = sdf.format(data.getReportDate());

			builder.addRow(mapRow(data.getClientId(), data.getDealerId(), data.getMetricAll(), data.getQueriesDirect(),
					data.getQueriesIndirect(), data.getQueriesChain(), data.getViewsMaps(), data.getViewsSearch(), data.getActionsWebsite(),
					data.getActionsPhone(), data.getActionsDrivingDirections(), data.getPhotosViewsMerchant(), data.getPhotosViewsCustomers(), 
					data.getPhotosCountMerchant(), data.getPhotosCountCustomers(), data.getLocalPostViewsSearch(),
					data.getLocalPostActionsCallToAction(),data.getBusinessImpressionsDesktopMaps(),
					data.getBusinessImpressionsDesktopSearch(), data.getBusinessImpressionsMoblieMaps() , data.getBusinessImpressionsMoblieSearch(),
					reportDateStr));
		}
		InsertAllResponse response = bigquery.insertAll(builder.build());
		if (response.hasErrors()) {
			String error = "";
			for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
				error += entry.getValue() + " | ";
			}
			error = error.trim();
			throw new RuntimeException(error);
		}
		return response;
	}
	
	public void delete(BigQuery bigQuery, String clientName, Date startDate, Date insightsEndDate, String dealerIds) throws FileNotFoundException, IOException, JobException, InterruptedException {
		String startDateStr = sdf.format(startDate);
		String endDateStr = sdf.format(insightsEndDate);

		String tableName = GMBLocationInsight.SQL_TABLE .substring(0, GMBLocationInsight.SQL_TABLE.length() - 4);
		String query = "delete FROM `"+ PROJECT_ID + "."+ clientName + "." + tableName + "` where " + GMBLocationInsight.SQL_COLUMN_REPORT_DATE + " <= '" + endDateStr + "' and " + GMBLocationInsight.SQL_COLUMN_REPORT_DATE +" >= '"+ startDateStr+"' and " + GMBLocationInsight.SQL_COLUMN_DEALER_ID +" in("+ dealerIds+")";
			//	"delete FROM `"+ PROJECT_ID + "."+ clientName + "." + tableName + "` where account_id = '" + accountID  + "' and change_date_time <= '" + sdf .format(endDate) + "' and change_date_time >= '"  + sdf.format(startDate) + "'";
		//String query = "delete FROM `"+ PROJECT_ID + "."+ clientName + "." + tableName + "` where " + GMBLocationInsight.SQL_COLUMN_LOCATION_ID + " = " + locationId + " and " + GMBLocationInsight.SQL_COLUMN_REPORT_DATE + " = '" + reportDateStr + "'";
		System.out.println(query);
		QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
		bigQuery.query(queryConfig);
	}
	
	public void deleteByDealerId(BigQuery bigQuery, String clientName, String dealerId) throws FileNotFoundException, IOException, JobException, InterruptedException {
		String tableName = GMBLocationInsight.SQL_TABLE .substring(0, GMBLocationInsight.SQL_TABLE.length() - 4);
		String query = "delete FROM `"+ PROJECT_ID + "."+ clientName + "." + tableName + "` where dealer_id = '"+dealerId+"'";
		System.out.println(query);
		QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
		bigQuery.query(queryConfig);
	}
	
	public void deleteByReportDate(BigQuery bigQuery, String clientName, Date startDate, Date endDate) throws FileNotFoundException, IOException, JobException, InterruptedException {
		String tableName = GMBLocationInsight.SQL_TABLE .substring(0, GMBLocationInsight.SQL_TABLE.length() - 4);
		String query = "delete FROM `"+ PROJECT_ID + "."+ clientName + "." + tableName + "` where report_date >= '"+startDate+"' and report_date <= '"+endDate+"'";
		System.out.println(query);
		QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
		bigQuery.query(queryConfig);
}
	public void update(BigQuery bigQuery, String clientName, String newDealerId, String dealerId) throws FileNotFoundException, IOException, JobException, InterruptedException {

		String tableName = GMBLocationInsight.SQL_TABLE .substring(0, GMBLocationInsight.SQL_TABLE.length() - 4);
		String query = "update `"+ PROJECT_ID + "."+ clientName + "." + tableName + "` set dealer_id = '" +newDealerId+"' where dealer_id = '"+dealerId+"'";
		System.out.println(query);
		QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
		bigQuery.query(queryConfig);
	}
	
	public Map<String, Object> mapRow(String clientId, String dealerId, long metricAll, long queriesDirect,
			long queriesIndirect, long queriesChain, long viewsMaps, long viewsSearch, long actionsWebsite,
			long actionsPhone, long actionsDrivingDirections, long photosViewsMerchant, long photosViewsCustomers,
			long photosCountMerchant, long photosCountCustomers, long localPostViewsSearch,
			long localPostActionsCallToAction, long businessImpressionsDesktopMaps,
			long businessImpressionsDesktopSearch, long businessImpressionsMoblieMaps,
			long businessImpressionsMoblieSearch, String reportDate) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(GMBLocationInsight.SQL_COLUMN_CLIENT_ID, clientId);
		map.put(GMBLocationInsight.SQL_COLUMN_DEALER_ID, dealerId);
		map.put(GMBLocationInsight.SQL_COLUMN_METRIC_ALL, metricAll);
		map.put(GMBLocationInsight.SQL_COLUMN_QUERIES_DIRECT, queriesDirect);
		map.put(GMBLocationInsight.SQL_COLUMN_QUERIES_INDIRECT, queriesIndirect);
		map.put(GMBLocationInsight.SQL_COLUMN_QUERIES_CHAIN, queriesChain);
		map.put(GMBLocationInsight.SQL_COLUMN_VIEWS_MAPS, viewsMaps);
		map.put(GMBLocationInsight.SQL_COLUMN_VIEWS_SEARCH, viewsSearch);
		
		map.put(GMBLocationInsight.SQL_COLUMN_ACTIONS_WEBSITE, actionsWebsite);
		map.put(GMBLocationInsight.SQL_COLUMN_ACTIONS_PHONE, actionsPhone);
		map.put(GMBLocationInsight.SQL_COLUMN_ACTIONS_DRIVING_DIRECTIONS, actionsDrivingDirections);
		map.put(GMBLocationInsight.SQL_COLUMN_PHOTOS_VIEWS_MERCHANT, photosViewsMerchant);
		map.put(GMBLocationInsight.SQL_COLUMN_PHOTOS_VIEWS_CUSTOMERS, photosViewsCustomers);
		map.put(GMBLocationInsight.SQL_COLUMN_PHOTOS_COUNT_MERCHANT, photosCountMerchant);
		map.put(GMBLocationInsight.SQL_COLUMN_PHOTOS_COUNT_CUSTOMERS, photosCountCustomers);
		map.put(GMBLocationInsight.SQL_COLUMN_LOCAL_POST_VIEWS_SEARCH, localPostViewsSearch);
		map.put(GMBLocationInsight.SQL_COLUMN_LOCAL_POST_ACTIONS_CALL_TO_ACTION, localPostActionsCallToAction);
		
		map.put(GMBLocationInsight.SQL_COLUMN_BUSINESS_IMPRESSIONS_DESKTOP_MAPS, businessImpressionsDesktopMaps);
		map.put(GMBLocationInsight.SQL_COLUMN_BUSINESS_IMPRESSIONS_DESKTOP_SEARCH, businessImpressionsDesktopSearch);
		map.put(GMBLocationInsight.SQL_COLUMN_BUSINESS_IMPRESSIONS_MOBILE_MAPS, businessImpressionsMoblieMaps);
		map.put(GMBLocationInsight.SQL_COLUMN_BUSINESS_IMPRESSIONS_MOBILE_SEARCH, businessImpressionsMoblieSearch);

		map.put(GMBLocationInsight.SQL_COLUMN_REPORT_DATE, reportDate);

		return map;
	}

}
