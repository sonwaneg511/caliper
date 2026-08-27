package com.caliper.task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.caliper.bigquery.service.BigQueryConfig;
import com.caliper.location.entity.Client;
import com.caliper.location.gmb.entity.GMBAccount;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.service.GMBAccountService;
import com.caliper.location.gmb.service.GMBLocationService;
import com.caliper.location.gmb.service.GMBSessionFactory;
import com.caliper.location.service.ClientService;
import com.caliper.reporting.entity.GMBInsightSettings;
import com.caliper.reporting.entity.GMBLocationInsight;
import com.caliper.reporting.service.GMBInsightService;
import com.google.api.services.businessprofileperformance.v1.BusinessProfilePerformance;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.JobException;

@Configuration
public class GMBInsightDeploymentTask extends ParameterizedJob {

    @Autowired
    private GMBInsightService gmbInsightService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private GMBLocationService gmbLocationService;

    @Autowired
    private GMBSessionFactory gmbSessionFactory;

    @Autowired
    private BigQueryConfig bigQueryConfig;

    @Autowired
    private GMBAccountService gmbAccountSerive;
    

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private Date insightsEndDate;
    private boolean fetchGMBInsight;
    private boolean insertSQL;
    private boolean insertBQ;
    private BigQuery bigQuery;
    private Date lastProcessingDate;
    private List<Client> clientList;
	private int batchSize;

//    @Override
//    public void run() {
//        try {
//            init();
//
//            if (!fetchGMBInsight) {
//                return;
//            }
//
//            int threads = Math.min(5, clientList.size());
//            ExecutorService executor = Executors.newFixedThreadPool(threads);
//
//            List<Future<?>> futures = new ArrayList<>();
//
//            for (Client client : clientList) {
//            	     futures.add(executor.submit(() -> {
//            	try {
//            		processClient(client);
//            	} catch (Exception e) {
//            		log("Error processing client : " + client.getClientId());
//            		e.printStackTrace();
//            	}
//            	 }));
//            }
//
//            for (Future<?> f : futures) {
//                f.get();
//            }
//
//            executor.shutdown();
//            log("End of task");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    
	@Override
	public void run() {
	    try {
	        init();
	        if (!fetchGMBInsight) return;

	        List<CompletableFuture<Void>> futures = clientList.stream()
	                .map(client -> gmbInsightService.processClientAsync(this, client))
	                .toList();

	        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

	        log("End of task");

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}


    /* ================= CLIENT LEVEL ================= */

    public void processClient(Client client) throws Exception {

        log("Fetching Insights for clientId :: " + client.getClientId());

        BusinessProfilePerformance businessPerformance = gmbSessionFactory.getMyBusinessProfile(client.getClientId());

        List<GMBAccount> gmbAccounts = gmbAccountSerive.findByClientId(client.getClientId());

        Map<String, Date> dealerIdVsDate = new HashMap<>();
        List<GMBInsightSettings> settings = gmbInsightService.findByClientId(client.getClientId());

        if (settings != null) {
        	dealerIdVsDate = settings.stream().collect(Collectors.toMap(GMBInsightSettings::getDealerId, GMBInsightSettings::getLastInsertedDate));
        }

        //Required for Map to set Dealer Id while returning the response
        List<GMBLocation> allLocations = gmbLocationService.getAllGmbLocationByClientId(client.getClientId());

        Map<String, String> locationVsDealerIdMap = new HashMap<>();
        for (GMBLocation loc : allLocations) {
            locationVsDealerIdMap.put(loc.getGmbLocationId(), loc.getDealerId());
        }
        //---------------------------------------------------------------------------------------
        Map<String, List<GMBLocation>> accountToLocationMap =
                allLocations.stream()
                        .collect(Collectors.groupingBy(GMBLocation::getAccountId));

        for (GMBAccount account : gmbAccounts) {
            processAccount(client, account, accountToLocationMap, dealerIdVsDate, locationVsDealerIdMap, businessPerformance);
        }
    }

    /* ================= ACCOUNT LEVEL ================= */

    private void processAccount(Client client, GMBAccount gmbAccount, Map<String, List<GMBLocation>> accountToLocationMap, Map<String, Date> dealerIdVsDate, Map<String, String> locationVsDealerIdMap, BusinessProfilePerformance businessPerformance) throws Exception {

        List<GMBLocation> locations = accountToLocationMap.get(gmbAccount.getAccountId());

        if (locations == null || locations.isEmpty()) {
            return;
        }

        log("Processing Account " + gmbAccount.getAccountId() + " | Locations " + locations.size());

        List<GMBLocationInsight> allInsights = new ArrayList<>();
        Set<String> processedDealerIds = new HashSet<>();

        for (GMBLocation location : locations) {

            if (GMBLocation.LOCATION_STATE_DUPLICATE .equalsIgnoreCase(location.getStatus())) {
                continue;
            }

            Date lastProcessedDate = DateUtils.truncate(DateUtils.addDays(new Date(), -10), Calendar.DATE);

            if (dealerIdVsDate.get(location.getDealerId()) != null) {
                lastProcessedDate = dealerIdVsDate.get(location.getDealerId());
            }

            if (!lastProcessedDate.before(insightsEndDate)) {
                continue;
            }

            Date startDate = DateUtils.truncate(DateUtils.addDays(lastProcessedDate, -10), Calendar.DATE);

            Map<Date, GMBLocationInsight> processLocation;

            try {
                Thread.sleep(1000);
                processLocation = gmbInsightService.processLocation(businessPerformance, location.getGmbLocationId(), startDate, insightsEndDate, locationVsDealerIdMap, logger);
            } catch (Exception ex) {
                Thread.sleep(5000);
                continue;
            }

            for (Map.Entry<Date, GMBLocationInsight> entry : processLocation.entrySet()) {

                GMBLocationInsight insight = entry.getValue();//-------------change variable name
                insight.setClientId(client.getClientId());
                insight.setReportDate(entry.getKey());
                insight.setDealerId(location.getDealerId());

                long maps = insight.getBusinessImpressionsDesktopMaps() + insight.getBusinessImpressionsMoblieMaps();
                long search = insight.getBusinessImpressionsDesktopSearch() + insight.getBusinessImpressionsMoblieSearch();

                insight.setViewsMaps(maps);
                insight.setViewsSearch(search);

                allInsights.add(insight);
                processedDealerIds.add(location.getDealerId());
            }

            if (processedDealerIds.size() % batchSize == 0) {//---------add the batch in parameters
                insertInsights(allInsights, client.getClientId());
                allInsights.clear();
                processedDealerIds.clear();
            }
        }

        if (!allInsights.isEmpty()) {
            insertInsights(allInsights, client.getClientId());
        }
    }

    /* ================= INSERT ================= */

    private void insertInsights(List<GMBLocationInsight> allLocationInsights, String clientId) throws FileNotFoundException, JobException, IOException, InterruptedException, SQLException {
		log("Will update DB");
		Set<String> dealerIdsSet = allLocationInsights.stream().map(e->String.valueOf("'"+e.getDealerId()+"'")).collect(Collectors.toSet());
		Set<String> dealerIdsUpdateSet = allLocationInsights.stream().map(e->String.valueOf(e.getDealerId())).collect(Collectors.toSet());
		String dealerIds = "";
		dealerIds = String.join(",", dealerIdsSet);
		Date minStartDate = allLocationInsights.stream().map(GMBLocationInsight::getReportDate).min(Date::compareTo).get();

		if(dealerIds.endsWith(",")) {
			int lastIndexOf = dealerIds.lastIndexOf(",");
			dealerIds = dealerIds.substring(0, lastIndexOf);
		}

		if(dealerIds.startsWith(",")) {
			dealerIds = dealerIds.substring(1, dealerIds.length());
		}
		System.out.println("dealerIds = "+dealerIds);


		if (insertSQL) {
			log("Processing SQL");

			log("Deleting from SQL for start date = "+minStartDate +" end date = "+insightsEndDate + " : locations - " + dealerIds);
			gmbInsightService.deleteGmbLocationInisghtByReportDateAndDealerId(clientId, minStartDate, insightsEndDate, dealerIdsUpdateSet);

			log("Inserting in SQL Size :: "+allLocationInsights.size());
			gmbInsightService.saveBatchGmbLocationInsight(allLocationInsights);
		}

//		if(insertBQ) {
//			log("Deleting from BQ for start date = "+minStartDate +" end date = "+insightsEndDate + " : locations - " + dealerIds);
//			gmbInsightService.deleteByReportDateBQ(bigQuery, clientId, minStartDate, insightsEndDate, dealerIds);
//
//			log("Inserting in BQ Size :: "+allLocationInsights.size());
//			gmbInsightService.insertGMBLocationInsightBQ(bigQuery, clientId, allLocationInsights);
//		}


		List<GMBInsightSettings> insightSettings = gmbInsightService.findByClientId(clientId);
		List<String> existingLocsInSettings = insightSettings.stream().filter(e->e.getClientId().equalsIgnoreCase(clientId)).map(e->e.getDealerId()).collect(Collectors.toList());
		Set<String> dealerIdsSetIns = allLocationInsights.stream().map(e->String.valueOf(e.getDealerId())).collect(Collectors.toSet());
		for(String dealerId : dealerIdsSetIns) {
			if(!existingLocsInSettings.contains(dealerId)) {
				gmbInsightService.saveGMBInsightSettings(clientId, dealerId, lastProcessingDate);
			}
		}

		int count = gmbInsightService.updateGMBInsightSettingsByDealerId(insightsEndDate, clientId, dealerIdsUpdateSet);
		
		System.out.println(count);

	}
    

    /* ================= INIT ================= */

    private void init() throws IOException, ParseException {

        String clientId = parameters.getString("client-id", null);
        if (clientId != null && !clientId.isBlank()) {
            this.clientList = List.of(clientService.findByClientId(clientId));
        } else {
            this.clientList = clientService.findAllClient();
        }
        this.fetchGMBInsight = parameters.getBoolean("fetch-gmb-insight", false);
        this.insertSQL = parameters.getBoolean("insert-SQL", false);
        this.insertBQ = parameters.getBoolean("insert-BQ", false);
        this.batchSize = parameters.getInt("batch-size",100);

        this.insightsEndDate = parameters.getDate("insights-end-date", DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DATE), -4));

//        this.bigQuery = bigQueryConfig.getBigQueryDao();

        this.lastProcessingDate = parameters.getDate("last-processing-date", DateUtils.addYears(DateUtils.truncate(new Date(), Calendar.DATE), -1));
    }
}
