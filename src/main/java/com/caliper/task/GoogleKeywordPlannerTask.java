package com.caliper.task;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.caliper.keywordPlanner.dto.GoogleKeywordEstimate;
import com.caliper.keywordPlanner.dto.GoogleMonthlySearches;
import com.caliper.keywordPlanner.entity.SearchVolumeData;
import com.caliper.keywordPlanner.entity.SearchVolumeKeywords;
import com.caliper.keywordPlanner.entity.SearchVolumeLocations;
import com.caliper.keywordPlanner.entity.SearchVolumePlan;
import com.caliper.keywordPlanner.repository.GoogleExpansionRepository;
import com.caliper.keywordPlanner.repository.SearchVolumeDataRepository;
import com.caliper.keywordPlanner.repository.SearchVolumeKeywordsRepository;
import com.caliper.keywordPlanner.repository.SearchVolumeLocationsRepository;
import com.caliper.keywordPlanner.repository.SearchVolumePlanRepository;
import com.caliper.keywordPlanner.service.KeywordService;
import com.caliper.utils.campaign.GoogleSessionFactory;
import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v21.enums.KeywordPlanNetworkEnum.KeywordPlanNetwork;

@Service
public class GoogleKeywordPlannerTask extends ParameterizedJob{
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

	@Autowired
	private KeywordService keywordService;
	private GoogleAdsClient googleAdsClient;

	private boolean createPlan;
	private boolean populateDataInPlan;
	private boolean generateReport;
	private boolean deleteExpiredReports;
	private long retryCount;
	private long batchSize =1;
	private long customerId = 3132737552L;
	private boolean keywordExpansion;
	private boolean searchVolume;

	@Autowired
	private SearchVolumePlanRepository searchVolumePlanRepository;

	@Autowired
	private SearchVolumeDataRepository searchVolumeDataRepository;

	@Autowired
	private SearchVolumeKeywordsRepository searchVolumeKeywordsRepository;

	@Autowired
	private SearchVolumeLocationsRepository searchVolumeLocationsRepository;

	@Autowired
	private GoogleExpansionRepository googleExpansionRepository;

	@Override
	public void run() {
		try {
			init();
					createPlan();
					populateDataInPlan();
					generateKeywordExpansion();
					generateReport();
					deleteExpiredReports();

		} catch (Exception ex) {
			ex.printStackTrace();
			//logException("Exception : ", ex);
		}
	}

//	protected void executeJob(JobExecutionContext context) throws Exception {
//
//
//	}

	private void createPlan() throws SQLException {
		if (!createPlan || !searchVolume) {
			return;
		}

		List<SearchVolumePlan> allPlans = searchVolumePlanRepository.findAll();
		if(CollectionUtils.isEmpty(allPlans)) {
			return;
		}

		for(SearchVolumePlan searchVolumePlan:allPlans) {
			if(!searchVolumePlan.isPlanCreated() && searchVolumePlan.getPlanType().equalsIgnoreCase(SearchVolumePlan.PLAN_TYPE_SEARCH_VOLUME)) {

				Long planId = searchVolumePlan.getPlanId();
				log("Creating Plans for Plan Id : "+planId);
				List<SearchVolumeData> searchVolumeData	= searchVolumeDataRepository.findAllByPlanID(planId);

				Set<KeywordLocationMapping> keywordLocationMappingSet = new HashSet<KeywordLocationMapping>();
				for(SearchVolumeData data:searchVolumeData) {

					keywordLocationMappingSet.add(new KeywordLocationMapping(data.getLocation(),data.getKeyword()));
				}

				List<SearchVolumeKeywords> searchVolumeKeywords = searchVolumeKeywordsRepository.findAllByPlanID(planId);
				List<SearchVolumeLocations> searchVolumeLocations = searchVolumeLocationsRepository.findAllByPlanID(planId);

				for(SearchVolumeKeywords searchVolumeKeyword : searchVolumeKeywords) {
					for(SearchVolumeLocations searchVolumeLocation : searchVolumeLocations) {
						String keyword = searchVolumeKeyword.getKeyword();
						String location = searchVolumeLocation.getLocation();
						KeywordLocationMapping newKeywordLocationData = new KeywordLocationMapping(location,keyword);
						if(!keywordLocationMappingSet.contains(newKeywordLocationData)) {
							SearchVolumeData newData = new SearchVolumeData();
							newData.setPlanID(planId);
							newData.setKeyword(keyword);
							newData.setLocation(location);
							newData.setFetchSuccess(false);
							searchVolumeDataRepository.save(newData);
						}
					}
				}

				searchVolumePlan.setPlanCreated(true);
				searchVolumePlanRepository.save(searchVolumePlan);
			}
		}
		System.out.println("Created plans");
	}

	private void generateKeywordExpansion() throws Exception {

		if(!keywordExpansion) {
			return;
		}

		List<SearchVolumePlan> allPlans = searchVolumePlanRepository.findAllByIsReportReadyAndPlanType(false, SearchVolumePlan.PLAN_TYPE_KEYWORD_EXPANSION);
		if(CollectionUtils.isEmpty(allPlans)) {
			return;
		}

		for(SearchVolumePlan searchVolumePlan : allPlans) {
			log("Search Volume Plan : " + searchVolumePlan.getName());
			if(searchVolumePlan.getRetryCount()>0) {
				continue;
			}

				Long planId = searchVolumePlan.getPlanId();
				String keywordExpansionType = searchVolumePlan.getKeywordExpansionType();
				String source = searchVolumePlan.getSource();
				String keywordPlanNetwork = searchVolumePlan.getKeywordPlanNetwork();
				log("keywordPlanNetwork : " + keywordPlanNetwork);
				log("Search Volume Plan Id : " + planId);

				List<SearchVolumeKeywords> searchVolumeKeywords = searchVolumeKeywordsRepository.findAllByFetchSuccessAndPlanID(false, planId);

				for(SearchVolumeKeywords keyword : searchVolumeKeywords) {
					log(String.valueOf("Processing Keyword - "+keyword.getKeyword()));
					keywordService.expandGoogleKeyword(planId, googleAdsClient, String.valueOf(keyword.getKeyword()) , 0, 0, String.valueOf(customerId), keywordExpansionType, source, keywordPlanNetwork);
					log("Keyword Processed : "+keyword.getKeyword());
					Thread.sleep(1000);

					keyword.setFetchSuccess(true);
					searchVolumeKeywordsRepository.save(keyword);
			}

				searchVolumePlan.setPlanCreated(true);
				searchVolumePlan.setRetryCount(this.retryCount);
				searchVolumePlanRepository.save(searchVolumePlan);
		}
	}
	private void populateDataInPlan() throws Exception {

		if (!populateDataInPlan || !searchVolume) {
			return;
		}

		List<SearchVolumePlan> searchVolumePlan = searchVolumePlanRepository.findAllByIsReportReadyAndPlanType(false, SearchVolumePlan.PLAN_TYPE_SEARCH_VOLUME);
		log("Got plans - " + searchVolumePlan.size());

		for(SearchVolumePlan plan : searchVolumePlan) {
			long volumePlanId = plan.getPlanId();
			log("Populating data for plan id - " + volumePlanId);

			KeywordPlanNetwork keywordPlanNetwork = KeywordPlanNetwork.valueOf(plan.getKeywordPlanNetwork());
			List<SearchVolumeData> searchVolumeData = searchVolumeDataRepository.findAllByPlanIDAndFetchSuccess(volumePlanId, false);
			log("Got search volume data without fetch success - " + searchVolumeData.size());


			if(CollectionUtils.isEmpty(searchVolumeData)) {
				long count = plan.getRetryCount();
				count++;
				plan.setRetryCount(count);
				searchVolumePlanRepository.save(plan);
				continue;
			}

			Map<Long, List<SearchVolumeData>> planIdVsSearchVolume = searchVolumeData
					.stream()
					.collect(
							Collectors.groupingBy(SearchVolumeData::getPlanID)
							);

			log("Got map - " + planIdVsSearchVolume.size());
			for(Entry<Long, List<SearchVolumeData>> data : planIdVsSearchVolume.entrySet()) {
				Long planId = data.getKey();
				long count = plan.getRetryCount();
				log("Executing plan - " + planId + " : Retry count - " + count + " : Keywords - " + data.getValue().size());

				if(count<retryCount) {


					if(searchVolumeData.size()>300000) {

						batchSize = 299999;
					}else if(searchVolumeData.size()<10){

						batchSize = 1;
					}

					else {
						System.out.println("searchVolumeData.size() :"+searchVolumeData.size());
						batchSize = (Math.round(searchVolumeData.size()/10.0));
					}
					if(count == (retryCount-1) && batchSize>5000) {
						batchSize = 4999;
					}
					Map<String, Set<String>> locationVsKeywordMap = new HashMap<>();
					Set<String> keywordSet = new HashSet<>();
					for(SearchVolumeData volumeData:data.getValue()) {
						String location = volumeData.getLocation();
						String keyword = volumeData.getKeyword();
						keywordSet = locationVsKeywordMap.getOrDefault(location, new HashSet<>());
						keywordSet.add(keyword);
						locationVsKeywordMap.put(location, keywordSet);
					}
					log("Got location map - " + locationVsKeywordMap.size());

					long batchCount = 1;
					long cnt = 1;
					for (Entry<String, Set<String>> cityVsKeyword : locationVsKeywordMap.entrySet()) {
						String city = cityVsKeyword.getKey().trim().toLowerCase();
						Set<String> keywords = cityVsKeyword.getValue();

						Set<String> kwdsBatch = new HashSet<String>();
						for (Iterator<String> kwd = keywords.iterator(); kwd.hasNext();) {

							kwdsBatch.add(kwd.next());
							batchCount++;
							if((batchCount % batchSize) == 0) {
								cnt++;
								log("\nprocessing batch : " +batchCount+" Batch-size : "+batchSize);
								fetchAndUpdateVolumeData(keywordPlanNetwork, planId, city, kwdsBatch);
							}
						}
						if(kwdsBatch.size() > 0) {
							fetchAndUpdateVolumeData(keywordPlanNetwork, planId, city, kwdsBatch);
						}
					}
					log("batches created : " +cnt);
					count++;
					plan.setRetryCount(count);
					searchVolumePlanRepository.save(plan);
				}
			}
		}
	}

	private void fetchAndUpdateVolumeData(KeywordPlanNetwork keywordPlanNetwork,
			Long planId, String city, Set<String> kwdsBatch) throws Exception, ParseException, SQLException {

		if(!searchVolume) {
			return;
		}

		List<GoogleKeywordEstimate> keywordTrafficEstimates = keywordService.getKeywordsReachEstimate(kwdsBatch, city, keywordPlanNetwork, batchSize, String.valueOf(customerId));
		for(GoogleKeywordEstimate googleKeywordEstimate:keywordTrafficEstimates) {
			Long avgSearchVolume = googleKeywordEstimate.getSearchVolume();
			String keyword = googleKeywordEstimate.getPhrase();
			String location = googleKeywordEstimate.getLocation();
			List<GoogleMonthlySearches> monthlySearches = googleKeywordEstimate.getMonthlySearches();
			if(monthlySearches.size()<12) {
				continue;
			}

			String latestMonth = "01-" + monthlySearches.get(11).getMonth() + "-" + monthlySearches.get(11).getYear();
			Date latestDate = DATE_FORMAT.parse(latestMonth);

			SearchVolumeData volumeData = searchVolumeDataRepository
					.findByPlanIDAndKeywordAndLocation(planId, keyword, location)
					.orElseGet(() -> {
						SearchVolumeData newRow = new SearchVolumeData();
						newRow.setPlanID(planId);
						newRow.setKeyword(keyword);
						newRow.setLocation(location);
						return newRow;
					});

			volumeData.setAvgSearchVolume(avgSearchVolume);
			volumeData.setMonth1(monthlySearches.get(0).getCount());
			volumeData.setMonth2(monthlySearches.get(1).getCount());
			volumeData.setMonth3(monthlySearches.get(2).getCount());
			volumeData.setMonth4(monthlySearches.get(3).getCount());
			volumeData.setMonth5(monthlySearches.get(4).getCount());
			volumeData.setMonth6(monthlySearches.get(5).getCount());
			volumeData.setMonth7(monthlySearches.get(6).getCount());
			volumeData.setMonth8(monthlySearches.get(7).getCount());
			volumeData.setMonth9(monthlySearches.get(8).getCount());
			volumeData.setMonth10(monthlySearches.get(9).getCount());
			volumeData.setMonth11(monthlySearches.get(10).getCount());
			volumeData.setMonth12(monthlySearches.get(11).getCount());
			volumeData.setLatestMonth(latestDate);
			volumeData.setFetchSuccess(true);

			searchVolumeDataRepository.save(volumeData);
		}

		kwdsBatch.clear();
	}

	private void generateReport() throws SQLException, IOException {
		if (!generateReport) {
			return;
		}

		// TODO: disabled for now - depends on a file-upload-directory configuration
		// that doesn't exist yet (there is no Configuration.getFileUploadDirectory()
		// anywhere in this codebase). Re-enable once that config is available; the
		// original CSVWriter-based implementation (using the opencsv dependency
		// added to pom.xml) is left below for reference - note the per-plan loop
		// must use `continue` (not `return`) when a plan has no data, otherwise it
		// aborts every remaining plan in `allPlans`.

//		List<SearchVolumePlan> allPlans = searchVolumePlanRepository.findAll();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
//		for(SearchVolumePlan searchVolumePlan : allPlans) {
//			if(searchVolumePlan.getRetryCount() < retryCount ){
//				continue;
//			}
//
//			File file = new File(Configuration.getFileUploadDirectory()+File.separator+searchVolumePlan.getName()+".csv");
//			FileWriter outputfile = new FileWriter(file);
//			CSVWriter writer = new CSVWriter(outputfile);
//			long planId = searchVolumePlan.getPlanId();
//
//			if(searchVolumePlan.getPlanType().equalsIgnoreCase(SearchVolumePlan.PLAN_TYPE_SEARCH_VOLUME)) {
//				List<SearchVolumeData> searchVolumeData = searchVolumeDataRepository.findAllByPlanID(planId);
//				Date latestMonth = null;
//				for(SearchVolumeData volumeData:searchVolumeData) {
//					latestMonth = volumeData.getLatestMonth();
//					if(latestMonth!=null) {
//						break;
//					}
//				}
//				if(latestMonth==null) {
//					String[] data = {"No data found for any keyword"};
//					writer.writeNext(data);
//				}
//				else {
//					List<String> headers = new ArrayList<>();
//
//					headers.add("PlanID");
//					headers.add("Location");
//					headers.add("Keyword");
//					headers.add("Average Search Volume");
//					for(int i=12 ;i>=1 ;i--) {
//						Date addMonths = DateUtils.addMonths(latestMonth, -i);
//						headers.add(sdf.format(addMonths));
//					}
//					String[] headersArray = headers.toArray(new String[0]);
//					writer.writeNext(headersArray);
//
//					for(SearchVolumeData volumeData:searchVolumeData) {
//
//						String[] data = {volumeData.getPlanID().toString(),volumeData.getLocation(),volumeData.getKeyword(),volumeData.getAvgSearchVolume().toString(),
//								volumeData.getMonth1().toString(),volumeData.getMonth2().toString(),volumeData.getMonth3().toString(),volumeData.getMonth4().toString()
//								,volumeData.getMonth5().toString(),volumeData.getMonth6().toString(),volumeData.getMonth7().toString(),volumeData.getMonth8().toString()
//								,volumeData.getMonth9().toString(),volumeData.getMonth10().toString(),volumeData.getMonth11().toString(),volumeData.getMonth12().toString()};
//
//						writer.writeNext(data);
//					}
//				}
//			}
//
//			if(searchVolumePlan.getPlanType().equalsIgnoreCase(SearchVolumePlan.PLAN_TYPE_KEYWORD_EXPANSION)) {
//
//				List<GoogleExpansion> searchVolumeData = googleExpansionRepository.findAllByPlanID(planId);
//				if(CollectionUtils.isEmpty(searchVolumeData)) {
//					writer.flush();
//					writer.close();
//					continue;
//				}
//
//				List<String> headers = new ArrayList<>();
//
//				headers.add("Plan ID");
//				headers.add("Seed Keyword");
//				headers.add("Expanded Keyword");
//				headers.add("Search Volume");
//
//				String[] headersArray = headers.toArray(new String[0]);
//				writer.writeNext(headersArray);
//
//				for(GoogleExpansion googleExpansion : searchVolumeData) {
//					String[] data = {String.valueOf(googleExpansion.getPlanID()), googleExpansion.getSeedKeyword(), googleExpansion.getExpandedKeyword(), String.valueOf(googleExpansion.getSearchVolume())};
//
//					writer.writeNext(data);
//				}
//			}
//			writer.flush();
//			writer.close();
//			searchVolumePlan.setReportReady(true);
//			searchVolumePlanRepository.save(searchVolumePlan);
//			System.out.println("Report generated for plan id - "+searchVolumePlan.getName());
//		}
	}

	private void deleteExpiredReports() throws SQLException {
		if (!deleteExpiredReports) {
			return;
		}

		List<SearchVolumePlan> allPlans = searchVolumePlanRepository.findAll();
		for(SearchVolumePlan searchVolumePlan:allPlans) {
			Long planId = searchVolumePlan.getPlanId();
			Date createdTime = searchVolumePlan.getCreatedTime();
			if(createdTime.before(DateUtils.addDays(new Date(), -15))) {
				System.out.println("Date older than 15 days" );
				searchVolumePlanRepository.deleteById(planId);
				searchVolumeKeywordsRepository.deleteAllByPlanID(planId);
				searchVolumeLocationsRepository.deleteAllByPlanID(planId);
				searchVolumeDataRepository.deleteAllByPlanID(planId);
			}
		}
	}

	private void init() throws IOException {
		this.createPlan = parameters.getBoolean("create-plan", false);
		this.populateDataInPlan = parameters.getBoolean("populate-data-in-plan", false);
		this.generateReport = parameters.getBoolean("generate-report", false);
		this.deleteExpiredReports = parameters.getBoolean("delete-expired-reports", false);
		this.retryCount = parameters.getInt("retry-count", 3);
		//		this.batchSize = params.getLong("batch-size");
		this.googleAdsClient = GoogleSessionFactory.getGoogleAdsClient();
		this.searchVolume = parameters.getBoolean("search-volume", false);
		this.keywordExpansion = parameters.getBoolean("keyword-expansion", false);
	}

	private class KeywordLocationMapping{

		public String location;
		public String keyword;

		public KeywordLocationMapping(String location, String keyword) {
			super();
			this.location = location;
			this.keyword = keyword;
		}

		public String getLocation() {
			return location;
		}

		public String getKeyword() {
			return keyword;
		}

		private GoogleKeywordPlannerTask getEnclosingInstance() {
			return GoogleKeywordPlannerTask.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + Objects.hash(keyword, location);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (getClass() != obj.getClass())
				return false;

			KeywordLocationMapping other = (KeywordLocationMapping) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			return keyword.equalsIgnoreCase(other.keyword) && location.equalsIgnoreCase(other.location);
		}
	}


}
