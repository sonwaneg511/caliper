//<<<<<<< Updated upstream
////package com.caliper.task;
////
////import java.io.FileNotFoundException;
////import java.io.IOException;
////import java.sql.SQLException;
////import java.text.ParseException;
////import java.text.SimpleDateFormat;
////import java.util.ArrayList;
////import java.util.Calendar;
////import java.util.Date;
////import java.util.HashMap;
////import java.util.HashSet;
////import java.util.List;
////import java.util.Map;
////import java.util.Set;
////import java.util.logging.Logger;
////import java.util.stream.Collectors;
////
////import org.apache.commons.lang3.time.DateUtils;
////import org.springframework.beans.factory.annotation.Autowired;
////	
////import com.caliper.job.runtime.ExecutableJob;
////import com.caliper.location.gmb.entity.GMBAccount;
////import com.caliper.location.gmb.entity.GMBLocation;
////import com.caliper.location.gmb.service.GMBInsightService;
////import com.caliper.location.gmb.service.GMBLocationService;
////import com.google.api.services.businessprofileperformance.v1.BusinessProfilePerformance;
////import com.google.cloud.bigquery.JobException;
////
////public class GMBInsightDeploymentTaskTest implements ExecutableJob{
////
////	private static final BusinessProfilePerformance businessPerformance = GMBSessionFactory.getMyBusinessProfile();
////	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
////	protected Logger logger = Logger.getLogger(this.getClass().toString() + "." + System.currentTimeMillis());
////	private Date insightsEndDate = DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DATE), -4);
////	
////	@Autowired
////	private GMBInsightService gmbInsightService;
////	
////	@Autowired
////	private GMBLocationService gmbLocationService;
////	
////	@Override
////	public void run(Map<String, String> params) throws Exception {
////		processInsights(params);
////	}
////	
////	private void processInsights(Map<String, String> params) throws SQLException, IOException, ParseException, JobException, InterruptedException {
////		
////		String clientId = params.get("client-id");
////		
////		
////		
////		
////		
////		try {
////			log("Fetching Insights");
////			//SEMClient semClientBySEMCilentName = semService.getSEMClientByTableName(clientName);
////			System.out.println("clientId :: "+clientId);
////			//System.out.println("semClientBySEMCilentName :: "+semClientBySEMCilentName.getClientType());
////			//long semClientId = semClientBySEMCilentName.getSemClientId();
////			List<GMBAccount> gmbAccounts = gmbService.getGMBAccountBySEMClientId(semClientId);
////
////			Map<String, Date> dealerIdVsDate = new HashMap<>();
////			List<GMBInsightSettings> gmbSettings =
////	                gmbInsightService.getInsightSettingByType(clientName, GMBInsightSettings.TYPE_INSIGHTS);
////			if(gmbSettings != null) {
////				for (GMBInsightSettings gmbSetting : gmbSettings) {
////					dealerIdVsDate.put(gmbSetting.getDealerId(), gmbSetting.getLastInsertedDate());
////				}
////			}
////
////			List<GMBLocation> allGMBLocations = gmbLocationService.getAllGmbLocationByClientId(clientId);
////
////			Map<String, Long> locationIdMap = new HashMap<String, Long>();
////			Map<String, String> locationVsDealerIdMap = new HashMap<>();
////
////			for (GMBLocation loc : allGMBLocations) {
////				locationIdMap.put(loc.getGmbLocationId(), loc.getId());
////				locationVsDealerIdMap.put(loc.getGmbLocationId(), loc.getDealerId());
////			}
////
////			Map<String, List<GMBLocation>> accountToLocationMap = allGMBLocations.stream()
////					.collect(Collectors.groupingBy(GMBLocation::getAccountId));
////
////			long progress = 0;
////			for (GMBAccount gmbAccount : gmbAccounts) {
////				String accountId = gmbAccount.getAccountId();
////				List<GMBLocation> locs = accountToLocationMap.get(accountId);
////				log("Processing Account- " + accountId + " : Locations - " + locs.size());
////				List<GMBLocationInsight> allLocationInsights = new ArrayList<GMBLocationInsight>();
////				Set<Long> processedLocationIds = new HashSet<Long>();
////
////				for (GMBLocation location : locs) {
////
////					String dealerId = location.getDealerId();
////					System.out.println("isStatus Duplicate = "+location.getStatus()+" for dealerId = "+location.getDealerId());
////					if (GMBLocation.LOCATION_STATE_DUPLICATE.equalsIgnoreCase(location.getStatus())) {
////						log("Skipping duplicate listing with id - " + dealerId);
////						continue;
////					}
////					Date lastProcessedDate = DateUtils.truncate(DateUtils.addDays(new Date(), -10), Calendar.DATE);
////					if(dealerIdVsDate != null && !dealerIdVsDate.isEmpty()) {
////						if(dealerIdVsDate.get(dealerId) != null) {
////							lastProcessedDate = dealerIdVsDate.get(dealerId);
////						}
////					}
////					if (!lastProcessedDate.before(insightsEndDate)) {
////						continue;
////					}
////
////					Date startDate = DateUtils.truncate(DateUtils.addDays(lastProcessedDate, -10), Calendar.DATE);
////
////					Map<Date, GMBLocationInsight> processLocation = new HashMap<Date, GMBLocationInsight>();
////					try {
////						Thread.sleep(1000);
////						log("Processing Location = " + location.getId()+" : dealer ID = "+location.getDealerId() + " : start date = " + startDate + " : end date = " + insightsEndDate);
////						processLocation = gmbInsightService.processLocation(businessPerformance, location.getGmbLocationId(), startDate, insightsEndDate, locationIdMap, locationVsDealerIdMap, logger);
////					} catch (Exception ex) {
////						// Do Nothing. Try Again.
////						//logException("Error fetching insights", ex);
////						log("Error fetching insights. Will sleep for 5 secs.");
////						Thread.sleep(5000);
////					}
////					List<GMBLocationInsight> gmbLocationInsightList = new ArrayList<>();
////
////					log("Iterating the Map for Location = "+dealerId);
////					for (Map.Entry<Date, GMBLocationInsight> entry : processLocation.entrySet()) {
////
////						Date insightDate = entry.getKey();
////						//log("Insight Date While Inserting :: "+insightDate);
////						GMBLocationInsight gmbLocationInsight = entry.getValue();
////
////						long processedLocationId = gmbLocationInsight.getLocationId();
////
////						long desktopMaps = gmbLocationInsight.getBusinessImpressionsDesktopMaps();
////						long mobileMaps = gmbLocationInsight.getBusinessImpressionsMoblieMaps();
////						long desktopSearch = gmbLocationInsight.getBusinessImpressionsDesktopSearch();
////						long mobileSearch = gmbLocationInsight.getBusinessImpressionsMoblieSearch();
////
////						long viewMaps = desktopMaps + mobileMaps;
////						long viewsSearch = desktopSearch + mobileSearch;
////
////						gmbLocationInsight.setViewsMaps(viewMaps);
////						gmbLocationInsight.setViewsSearch(viewsSearch);
////
////						gmbLocationInsight.setReportDate(insightDate);
////						gmbLocationInsightList.add(gmbLocationInsight);
////
////						processedLocationIds.add(processedLocationId);
////					}
////
////					allLocationInsights.addAll(gmbLocationInsightList);
////					//log("processedLocationIds - " + processedLocationIds.toString() + " : allLocationInsights - " + allLocationInsights.size());
////					if(allLocationInsights.size() > 0) {
////						log("allLocationInsights - " + allLocationInsights.size());
////
////						int batchSize = 20;
////						if(processedLocationIds.size() % batchSize == 0) {
////							log("Progressing " +batchSize+ " locations.. processedLocationIds - " + processedLocationIds.size() + " : allLocationInsights - " + allLocationInsights.size());
////
////							insertInsights(allLocationInsights);
////							processedLocationIds = new HashSet<Long>();
////							allLocationInsights = new ArrayList<GMBLocationInsight>();
////							//allLocationInsights.clear();
////						}
////						/*	if ((++progress % 10) == 0) {
////							log("Progress - " + progress + " : processedLocationIds - " + processedLocationIds.size() + " : allLocationInsights - " + allLocationInsights.size());
////						} */
////					}else {
////						log("No Insights fetched. Size = "+allLocationInsights.size());
////					}
////
////				}
////				if(processedLocationIds.size() > 0 && allLocationInsights.size() > 0) {
////					insertInsights(allLocationInsights);
////				}
////
////			}
////		} catch (Exception ex) {
////			logException("Exception while processing insights : " + ex.getMessage(), ex);
////		}
////	}
////
////	private void insertInsights(List<GMBLocationInsight> allLocationInsights) throws FileNotFoundException, JobException, IOException, InterruptedException, SQLException {
////		log("Will update DB");
////		//Set<String> locationIdsSet = allLocationInsights.stream().map(e->String.valueOf(e.getLocationId())).collect(Collectors.toSet());
////		Set<String> dealerIdsSet = allLocationInsights.stream().map(e->String.valueOf("'"+e.getDealerId()+"'")).collect(Collectors.toSet());
////		String dealerIds = "";
////		dealerIds = String.join(",", dealerIdsSet);
////		Date minStartDate = allLocationInsights.stream().map(GMBLocationInsight::getReportDate).min(Date::compareTo).get();
////
////		if(dealerIds.endsWith(",")) {
////			int lastIndexOf = dealerIds.lastIndexOf(",");
////			dealerIds = dealerIds.substring(0, lastIndexOf);
////		}
////
////		if(dealerIds.startsWith(",")) {
////			dealerIds = dealerIds.substring(1, dealerIds.length());
////		}
////		System.out.println("dealerIds = "+dealerIds);
////
////
////		if (insertSQL) {
////			log("Processing SQL");
////
////			log("Deleting from SQL for start date = "+minStartDate +" end date = "+insightsEndDate + " : locations - " + dealerIds);
////			gmbInsightService.deleteByReportDate(clientName, minStartDate, insightsEndDate, dealerIds);
////
////			log("Inserting in SQL Size :: "+allLocationInsights.size());
////			gmbInsightService.batchInsertGMBLocationInsight(clientName, allLocationInsights);
////		}
////
////		if(insertBQ) {
////			log("Deleting from BQ for start date = "+minStartDate +" end date = "+insightsEndDate + " : locations - " + dealerIds);
////			gmbInsightService.deleteByReportDateBQ(bigQuery, clientName, minStartDate, insightsEndDate, dealerIds);
////
////			log("Inserting in BQ Size :: "+allLocationInsights.size());
////			gmbInsightService.insertGMBLocationInsightBQ(bigQuery, clientName, allLocationInsights);
////		}
////
////
////		List<GMBInsightSettings> insightSettings = gmbInsightService.getAllInsightSetting(clientName);
////		List<String> existingLocsInSettings = insightSettings.stream().filter(e->e.getType().equalsIgnoreCase(GMBInsightSettings.TYPE_INSIGHTS)).map(e->e.getDealerId()).collect(Collectors.toList());
////		Set<String> dealerIdsSetIns = allLocationInsights.stream().map(e->String.valueOf(e.getDealerId())).collect(Collectors.toSet());
////		for(String dealerId : dealerIdsSetIns) {
////			if(!existingLocsInSettings.contains(dealerId)) {
////				gmbInsightService.insertGMBInsightSettings(clientName, dealerId, lastProcessingDate, GMBInsightSettings.TYPE_INSIGHTS);
////			}
////		}
////
////		gmbInsightService.updateGMBInsightSettingsByDealerId(clientName, dealerIds, insightsEndDate, GMBInsightSettings.TYPE_INSIGHTS);
////
////	}
////
////	
////}
//=======
//package com.caliper.task;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.sql.SQLException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.logging.Logger;
//import java.util.stream.Collectors;
//
//import org.apache.commons.lang3.time.DateUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//	
//import com.caliper.job.runtime.ExecutableJob;
//import com.caliper.location.gmb.entity.GMBAccount;
//import com.caliper.location.gmb.entity.GMBInsightSettings;
//import com.caliper.location.gmb.entity.GMBLocation;
//import com.caliper.location.gmb.entity.GMBLocationInsight;
//import com.caliper.location.gmb.service.GMBInsightService;
//import com.caliper.location.gmb.service.GMBLocationService;
//import com.google.api.services.businessprofileperformance.v1.BusinessProfilePerformance;
//import com.google.cloud.bigquery.JobException;
//
//public class GMBInsightDeploymentTaskTest implements ExecutableJob{
//
//	private static final BusinessProfilePerformance businessPerformance = GMBSessionFactory.getMyBusinessProfile();
//	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//	protected Logger logger = Logger.getLogger(this.getClass().toString() + "." + System.currentTimeMillis());
//	private Date insightsEndDate = DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DATE), -4);
//	
//	@Autowired
//	private GMBInsightService gmbInsightService;
//	
//	@Autowired
//	private GMBLocationService gmbLocationService;
//	
//	@Override
//	public void run(Map<String, String> params) throws Exception {
//		processInsights(params);
//	}
//	
//	private void processInsights(Map<String, String> params) throws SQLException, IOException, ParseException, JobException, InterruptedException {
//		
//		String clientId = params.get("client-id");
//		
//		
//		
//		
//		
//		try {
//			log("Fetching Insights");
//			//SEMClient semClientBySEMCilentName = semService.getSEMClientByTableName(clientName);
//			System.out.println("clientId :: "+clientId);
//			//System.out.println("semClientBySEMCilentName :: "+semClientBySEMCilentName.getClientType());
//			//long semClientId = semClientBySEMCilentName.getSemClientId();
//			List<GMBAccount> gmbAccounts = gmbService.getGMBAccountBySEMClientId(semClientId);
//
//			Map<String, Date> dealerIdVsDate = new HashMap<>();
//			List<GMBInsightSettings> gmbSettings =
//	                gmbInsightService.getInsightSettingByType(clientName, GMBInsightSettings.TYPE_INSIGHTS);
//			if(gmbSettings != null) {
//				for (GMBInsightSettings gmbSetting : gmbSettings) {
//					dealerIdVsDate.put(gmbSetting.getDealerId(), gmbSetting.getLastInsertedDate());
//				}
//			}
//
//			List<GMBLocation> allGMBLocations = gmbLocationService.getAllGmbLocationByClientId(clientId);
//
//			Map<String, Long> locationIdMap = new HashMap<String, Long>();
//			Map<String, String> locationVsDealerIdMap = new HashMap<>();
//
//			for (GMBLocation loc : allGMBLocations) {
//				locationIdMap.put(loc.getGmbLocationId(), loc.getId());
//				locationVsDealerIdMap.put(loc.getGmbLocationId(), loc.getDealerId());
//			}
//
//			Map<String, List<GMBLocation>> accountToLocationMap = allGMBLocations.stream()
//					.collect(Collectors.groupingBy(GMBLocation::getAccountId));
//
//			long progress = 0;
//			for (GMBAccount gmbAccount : gmbAccounts) {
//				String accountId = gmbAccount.getAccountId();
//				List<GMBLocation> locs = accountToLocationMap.get(accountId);
//				log("Processing Account- " + accountId + " : Locations - " + locs.size());
//				List<GMBLocationInsight> allLocationInsights = new ArrayList<GMBLocationInsight>();
//				Set<Long> processedLocationIds = new HashSet<Long>();
//
//				for (GMBLocation location : locs) {
//
//					String dealerId = location.getDealerId();
//					System.out.println("isStatus Duplicate = "+location.getStatus()+" for dealerId = "+location.getDealerId());
//					if (GMBLocation.LOCATION_STATE_DUPLICATE.equalsIgnoreCase(location.getStatus())) {
//						log("Skipping duplicate listing with id - " + dealerId);
//						continue;
//					}
//					Date lastProcessedDate = DateUtils.truncate(DateUtils.addDays(new Date(), -10), Calendar.DATE);
//					if(dealerIdVsDate != null && !dealerIdVsDate.isEmpty()) {
//						if(dealerIdVsDate.get(dealerId) != null) {
//							lastProcessedDate = dealerIdVsDate.get(dealerId);
//						}
//					}
//					if (!lastProcessedDate.before(insightsEndDate)) {
//						continue;
//					}
//
//					Date startDate = DateUtils.truncate(DateUtils.addDays(lastProcessedDate, -10), Calendar.DATE);
//
//					Map<Date, GMBLocationInsight> processLocation = new HashMap<Date, GMBLocationInsight>();
//					try {
//						Thread.sleep(1000);
//						log("Processing Location = " + location.getId()+" : dealer ID = "+location.getDealerId() + " : start date = " + startDate + " : end date = " + insightsEndDate);
//						processLocation = gmbInsightService.processLocation(businessPerformance, location.getGmbLocationId(), startDate, insightsEndDate, locationIdMap, locationVsDealerIdMap, logger);
//					} catch (Exception ex) {
//						// Do Nothing. Try Again.
//						//logException("Error fetching insights", ex);
//						log("Error fetching insights. Will sleep for 5 secs.");
//						Thread.sleep(5000);
//					}
//					List<GMBLocationInsight> gmbLocationInsightList = new ArrayList<>();
//
//					log("Iterating the Map for Location = "+dealerId);
//					for (Map.Entry<Date, GMBLocationInsight> entry : processLocation.entrySet()) {
//
//						Date insightDate = entry.getKey();
//						//log("Insight Date While Inserting :: "+insightDate);
//						GMBLocationInsight gmbLocationInsight = entry.getValue();
//
//						long processedLocationId = gmbLocationInsight.getLocationId();
//
//						long desktopMaps = gmbLocationInsight.getBusinessImpressionsDesktopMaps();
//						long mobileMaps = gmbLocationInsight.getBusinessImpressionsMoblieMaps();
//						long desktopSearch = gmbLocationInsight.getBusinessImpressionsDesktopSearch();
//						long mobileSearch = gmbLocationInsight.getBusinessImpressionsMoblieSearch();
//
//						long viewMaps = desktopMaps + mobileMaps;
//						long viewsSearch = desktopSearch + mobileSearch;
//
//						gmbLocationInsight.setViewsMaps(viewMaps);
//						gmbLocationInsight.setViewsSearch(viewsSearch);
//
//						gmbLocationInsight.setReportDate(insightDate);
//						gmbLocationInsightList.add(gmbLocationInsight);
//
//						processedLocationIds.add(processedLocationId);
//					}
//
//					allLocationInsights.addAll(gmbLocationInsightList);
//					//log("processedLocationIds - " + processedLocationIds.toString() + " : allLocationInsights - " + allLocationInsights.size());
//					if(allLocationInsights.size() > 0) {
//						log("allLocationInsights - " + allLocationInsights.size());
//
//						int batchSize = 20;
//						if(processedLocationIds.size() % batchSize == 0) {
//							log("Progressing " +batchSize+ " locations.. processedLocationIds - " + processedLocationIds.size() + " : allLocationInsights - " + allLocationInsights.size());
//
//							insertInsights(allLocationInsights);
//							processedLocationIds = new HashSet<Long>();
//							allLocationInsights = new ArrayList<GMBLocationInsight>();
//							//allLocationInsights.clear();
//						}
//						/*	if ((++progress % 10) == 0) {
//							log("Progress - " + progress + " : processedLocationIds - " + processedLocationIds.size() + " : allLocationInsights - " + allLocationInsights.size());
//						} */
//					}else {
//						log("No Insights fetched. Size = "+allLocationInsights.size());
//					}
//
//				}
//				if(processedLocationIds.size() > 0 && allLocationInsights.size() > 0) {
//					insertInsights(allLocationInsights);
//				}
//
//			}
//		} catch (Exception ex) {
//			logException("Exception while processing insights : " + ex.getMessage(), ex);
//		}
//	}
//
//	private void insertInsights(List<GMBLocationInsight> allLocationInsights) throws FileNotFoundException, JobException, IOException, InterruptedException, SQLException {
//		log("Will update DB");
//		//Set<String> locationIdsSet = allLocationInsights.stream().map(e->String.valueOf(e.getLocationId())).collect(Collectors.toSet());
//		Set<String> dealerIdsSet = allLocationInsights.stream().map(e->String.valueOf("'"+e.getDealerId()+"'")).collect(Collectors.toSet());
//		String dealerIds = "";
//		dealerIds = String.join(",", dealerIdsSet);
//		Date minStartDate = allLocationInsights.stream().map(GMBLocationInsight::getReportDate).min(Date::compareTo).get();
//
//		if(dealerIds.endsWith(",")) {
//			int lastIndexOf = dealerIds.lastIndexOf(",");
//			dealerIds = dealerIds.substring(0, lastIndexOf);
//		}
//
//		if(dealerIds.startsWith(",")) {
//			dealerIds = dealerIds.substring(1, dealerIds.length());
//		}
//		System.out.println("dealerIds = "+dealerIds);
//
//
//		if (insertSQL) {
//			log("Processing SQL");
//
//			log("Deleting from SQL for start date = "+minStartDate +" end date = "+insightsEndDate + " : locations - " + dealerIds);
//			gmbInsightService.deleteByReportDate(clientName, minStartDate, insightsEndDate, dealerIds);
//
//			log("Inserting in SQL Size :: "+allLocationInsights.size());
//			gmbInsightService.batchInsertGMBLocationInsight(clientName, allLocationInsights);
//		}
//
//		if(insertBQ) {
//			log("Deleting from BQ for start date = "+minStartDate +" end date = "+insightsEndDate + " : locations - " + dealerIds);
//			gmbInsightService.deleteByReportDateBQ(bigQuery, clientName, minStartDate, insightsEndDate, dealerIds);
//
//			log("Inserting in BQ Size :: "+allLocationInsights.size());
//			gmbInsightService.insertGMBLocationInsightBQ(bigQuery, clientName, allLocationInsights);
//		}
//
//
//		List<GMBInsightSettings> insightSettings = gmbInsightService.getAllInsightSetting(clientName);
//		List<String> existingLocsInSettings = insightSettings.stream().filter(e->e.getType().equalsIgnoreCase(GMBInsightSettings.TYPE_INSIGHTS)).map(e->e.getDealerId()).collect(Collectors.toList());
//		Set<String> dealerIdsSetIns = allLocationInsights.stream().map(e->String.valueOf(e.getDealerId())).collect(Collectors.toSet());
//		for(String dealerId : dealerIdsSetIns) {
//			if(!existingLocsInSettings.contains(dealerId)) {
//				gmbInsightService.insertGMBInsightSettings(clientName, dealerId, lastProcessingDate, GMBInsightSettings.TYPE_INSIGHTS);
//			}
//		}
//
//		gmbInsightService.updateGMBInsightSettingsByDealerId(clientName, dealerIds, insightsEndDate, GMBInsightSettings.TYPE_INSIGHTS);
//
//	}
//
//	
//}
//>>>>>>> Stashed changes
