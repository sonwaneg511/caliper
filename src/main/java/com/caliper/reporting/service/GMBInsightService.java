package com.caliper.reporting.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.caliper.location.api.GMBInsightAPI;
import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.entity.Client;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.gmb.specification.GMBLocationInsightSpecification;
import com.caliper.location.service.DealerLocationService;
import com.caliper.reporting.dto.request.GMBLocationInisghtRequest;
import com.caliper.reporting.dto.response.GMBLocationInsightDailyActionsTrend;
import com.caliper.reporting.dto.response.GMBLocationInsightDailyViewsTrend;
import com.caliper.reporting.dto.response.GMBLocationInsightDealerViewsActionsTrend;
import com.caliper.reporting.dto.response.GMBLocationInsightMonthlyActionsTrend;
import com.caliper.reporting.dto.response.GMBLocationInsightMonthlyViews;
import com.caliper.reporting.dto.response.GMBLocationInsightResponse;
import com.caliper.reporting.entity.GMBInsightSettings;
import com.caliper.reporting.entity.GMBLocationInsight;
import com.caliper.reporting.repository.GMBInsightSettingsRepository;
import com.caliper.reporting.repository.GMBLocationInsightRepository;
import com.caliper.task.GMBInsightDeploymentTask;
import com.google.api.services.businessprofileperformance.v1.BusinessProfilePerformance;
import com.google.api.services.businessprofileperformance.v1.model.DailyMetricTimeSeries;
import com.google.api.services.businessprofileperformance.v1.model.DatedValue;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class GMBInsightService {

	@PersistenceContext
    private EntityManager entityManager;
	
	@Autowired
	private GMBInsightAPI gmbInsightAPI;
	
	@Autowired
	private GMBInsightSettingsRepository gmbInsightSettingsRepository;
	
	@Autowired
	private GMBLocationInsightRepository gmbLocationInsightRepository;

	@Autowired
	private DealerLocationService dealerLocationService;
	
	private	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public List<GMBInsightSettings> findByClientId(String clientId){
		return gmbInsightSettingsRepository.findByClientId(clientId);
	}
	
	public void saveBatchGmbLocationInsight(List<GMBLocationInsight> gmbLocationInsights) {
		gmbLocationInsightRepository.saveAll(gmbLocationInsights);
	}
	
	public void saveGMBInsightSettings(String clientId, String dealerId, Date lastProcessingDate) {
		
		GMBInsightSettings gmbInsightSettings = GMBInsightSettings.builder()
				.clientId(clientId)
				.dealerId(dealerId)
				.lastInsertedDate(lastProcessingDate)
				.build();
		gmbInsightSettingsRepository.save(gmbInsightSettings);
	}
	
	public void deleteGmbLocationInisghtByReportDateAndDealerId(String clientId, Date startDate, Date endDate, Set<String> dealerIdsSet) {
		gmbLocationInsightRepository.deleteGmbLocationInisghtByReportDateAndDealerId(clientId, startDate, endDate, dealerIdsSet);
	}

	@Transactional
    public int updateGMBInsightSettingsByDealerId(
            Date lastInsertedDate,
            String clientId,
            Set<String> dealerIds
    ) {
        // 🔥 CRITICAL: flush pending inserts first
        entityManager.flush();

        return gmbInsightSettingsRepository
                .updateGMBInsightSettingsByDealerId(
                        lastInsertedDate,
                        clientId,
                        dealerIds
                );
    }
	
	//------------------------------------------------------------Client ASYNC------------------------------------------------
	
	@Async("gmbInsightTaskExecutor")
    public CompletableFuture<Void> processClientAsync(GMBInsightDeploymentTask task, Client client) {

        try {
            task.processClient(client);
        } catch (Exception e) {
            task.log("Error processing client " + client.getClientId());
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(null);
    }

	
	//--------------------------------------------INSIGHT 3rd PARTY API-------------------------------------------
	public Map<Date,GMBLocationInsight> processLocation(BusinessProfilePerformance business, String locationId, Date startDate, Date endDate, Map<String, String> locationVsDealerIdMap, Logger log) throws IOException, ParseException {
		Map<Date,GMBLocationInsight> dateToInsight = new HashMap<>();

		List<String> dailyMetricsList = Arrays.asList(GMBLocationInsight.METRIC_TYPE_BUSINESS_DIRECTION_REQUESTS,GMBLocationInsight.METRIC_TYPE_CALL_CLICKS,GMBLocationInsight.METRIC_TYPE_WEBSITE_CLICKS,GMBLocationInsight.METRIC_TYPE_BUSINESS_IMPRESSIONS_DESKTOP_MAPS,GMBLocationInsight.METRIC_TYPE_BUSINESS_IMPRESSIONS_DESKTOP_SEARCH,GMBLocationInsight.METRIC_TYPE_BUSINESS_IMPRESSIONS_MOBILE_MAPS,GMBLocationInsight.METRIC_TYPE_BUSINESS_IMPRESSIONS_MOBILE_SEARCH);
		processMetric(business, locationId, startDate, endDate, dailyMetricsList, dateToInsight, locationVsDealerIdMap, log);

		return dateToInsight;
	}

	public void processMetric(BusinessProfilePerformance business, String locationId, Date startDate, Date endDate, List<String> dailyMetricsList, Map<Date,GMBLocationInsight> dateToInsight,  Map<String, String> locationVsDealerIdMap, Logger log) throws IOException, ParseException {

		List<DailyMetricTimeSeries> dailyMetricTimeSeries = gmbInsightAPI.getDailyMetrics(business, dailyMetricsList, locationId, startDate, endDate);
		for(DailyMetricTimeSeries metricTimeSeries : dailyMetricTimeSeries) {
			String dailyMetric = metricTimeSeries.getDailyMetric();
			List<DatedValue> datedValues = metricTimeSeries.getTimeSeries().getDatedValues();
			for(DatedValue datedValue : datedValues) {
				long value = 0;
				Set<String> keySet = datedValue.keySet();
				if(keySet.contains(GMBLocationInsight.METRIC_VALUE)){
					value = datedValue.getValue();
				}
				String dateString = datedValue.getDate().getYear().toString()+"-"+datedValue.getDate().getMonth().toString()+"-"+datedValue.getDate().getDay().toString();
				//log.info("Date From API Date String :: "+dateString);
				Date date = inputFormat.parse(dateString);
				//log.info("Date From API After Conversion :: "+date);				
				if(dailyMetric.equalsIgnoreCase(GMBLocationInsight.METRIC_TYPE_BUSINESS_IMPRESSIONS_DESKTOP_MAPS)) {
					if(dateToInsight.containsKey(date)) {
						GMBLocationInsight gmbLocationInsight = dateToInsight.get(date);
						gmbLocationInsight.setBusinessImpressionsDesktopMaps(value);
					}else {
						GMBLocationInsight gmbLocInsight = new GMBLocationInsight(); 
						gmbLocInsight.setBusinessImpressionsDesktopMaps(value);
						dateToInsight.put(date, gmbLocInsight);
					}
				}
				else if(dailyMetric.equalsIgnoreCase(GMBLocationInsight.METRIC_TYPE_BUSINESS_IMPRESSIONS_DESKTOP_SEARCH)) {
					if(dateToInsight.containsKey(date)) {
						GMBLocationInsight gmbLocationInsight = dateToInsight.get(date);
						gmbLocationInsight.setBusinessImpressionsDesktopSearch(value);
					}else {
						GMBLocationInsight gmbLocInsight = new GMBLocationInsight(); 
						gmbLocInsight.setBusinessImpressionsDesktopSearch(value);
						dateToInsight.put(date, gmbLocInsight);
					}
				}
				else if(dailyMetric.equalsIgnoreCase(GMBLocationInsight.METRIC_TYPE_BUSINESS_IMPRESSIONS_MOBILE_MAPS)) {
					if(dateToInsight.containsKey(date)) {
						GMBLocationInsight gmbLocationInsight = dateToInsight.get(date);
						gmbLocationInsight.setBusinessImpressionsMoblieMaps(value);
					}else {
						GMBLocationInsight gmbLocInsight = new GMBLocationInsight(); 
						gmbLocInsight.setBusinessImpressionsMoblieMaps(value);
						dateToInsight.put(date, gmbLocInsight);
					}
				}
				else if(dailyMetric.equalsIgnoreCase(GMBLocationInsight.METRIC_TYPE_BUSINESS_IMPRESSIONS_MOBILE_SEARCH)) {
					if(dateToInsight.containsKey(date)) {
						GMBLocationInsight gmbLocationInsight = dateToInsight.get(date);
						gmbLocationInsight.setBusinessImpressionsMoblieSearch(value);
					}else {
						GMBLocationInsight gmbLocInsight = new GMBLocationInsight(); 
						gmbLocInsight.setBusinessImpressionsMoblieSearch(value);
						dateToInsight.put(date, gmbLocInsight);
					}
				}
				else if(dailyMetric.equalsIgnoreCase(GMBLocationInsight.METRIC_TYPE_BUSINESS_DIRECTION_REQUESTS)) {
					if(dateToInsight.containsKey(date)) {
						GMBLocationInsight gmbLocationInsight = dateToInsight.get(date);
						gmbLocationInsight.setActionsDrivingDirections(value);
					}else {
						GMBLocationInsight gmbLocInsight = new GMBLocationInsight(); 
						gmbLocInsight.setActionsDrivingDirections(value);
						dateToInsight.put(date, gmbLocInsight);
					}
				}
				else if(dailyMetric.equalsIgnoreCase(GMBLocationInsight.METRIC_TYPE_CALL_CLICKS)) {
					if(dateToInsight.containsKey(date)) {
						GMBLocationInsight gmbLocationInsight = dateToInsight.get(date);
						gmbLocationInsight.setActionsPhone(value);
					}else {
						GMBLocationInsight gmbLocInsight = new GMBLocationInsight(); 
						gmbLocInsight.setActionsPhone(value);
						dateToInsight.put(date, gmbLocInsight);
					}
				}
				else if(dailyMetric.equalsIgnoreCase(GMBLocationInsight.METRIC_TYPE_WEBSITE_CLICKS)) {
					if(dateToInsight.containsKey(date)) {
						GMBLocationInsight gmbLocationInsight = dateToInsight.get(date);
						gmbLocationInsight.setActionsWebsite(value);
					}else {
						GMBLocationInsight gmbLocInsight = new GMBLocationInsight(); 
						gmbLocInsight.setActionsWebsite(value);
						dateToInsight.put(date, gmbLocInsight);
					}
				}
				GMBLocationInsight gmbLocationInsight = dateToInsight.get(date);

				//gmbLocationInsight.setDealerId(locationVsDealerIdMap.get(locationId));
				dateToInsight.put(date, gmbLocationInsight);
			}
		}
	}



	//------------------------------------------------REQUESTS------------------------------------------------
	public LocationFilterRequest locationFilterRequestCreate(GMBLocationInisghtRequest req) {

		LocationFilterRequest locationRequest = LocationFilterRequest.builder()
				.clientId(req.getClientId())
				.userId(req.getUserId())           
				.build();

		return locationRequest;
	}

	//-------------------------------------------------INSIGHT DATA---------------------------------------------
	public GMBLocationInsightResponse getLocationInsightData(GMBLocationInisghtRequest req) {

	    LocationFilterRequest locationRequest = locationFilterRequestCreate(req);
	    List<DealerLocation> locations =
	            dealerLocationService.getFilteredDealerLocation(locationRequest);

	    Map<String, DealerLocation> dealerIdVsLocationMap =
	            locations.stream()
	                    .collect(Collectors.toMap(
	                            DealerLocation::getDealerId,
	                            Function.identity()
	                    ));

	    List<GMBLocationInsight> insights =
	            gmbLocationInsightRepository.findAll(
	                    GMBLocationInsightSpecification.filterGmbLocationInsight(req)
	            );

	    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

	    GMBLocationInsightResponse response = new GMBLocationInsightResponse();

	    /* ---------------- MONTHLY VIEWS ---------------- */
	    List<GMBLocationInsightMonthlyViews> monthlyViews =
	            insights.stream()
	                    .collect(Collectors.groupingBy(
	                            i -> toLocalDate(i.getReportDate()).format(monthFormatter),
	                            Collectors.summingLong(this::totalViews)
	                    ))
	                    .entrySet().stream()
	                    .sorted(Map.Entry.comparingByKey())
	                    .map(e -> {
	                        GMBLocationInsightMonthlyViews m = new GMBLocationInsightMonthlyViews();
	                        m.setMonth(e.getKey());
	                        m.setViewsCount(e.getValue());
	                        return m;
	                    })
	                    .toList();

	    /* ---------------- MONTHLY ACTIONS ---------------- */
	    List<GMBLocationInsightMonthlyActionsTrend> monthlyActionsTrend =
	            insights.stream()
	                    .collect(Collectors.groupingBy(
	                            i -> toLocalDate(i.getReportDate()).format(monthFormatter)
	                    ))
	                    .entrySet().stream()
	                    .sorted(Map.Entry.comparingByKey())
	                    .map(e -> {
	                        List<GMBLocationInsight> list = e.getValue();
	                        GMBLocationInsightMonthlyActionsTrend t =
	                                new GMBLocationInsightMonthlyActionsTrend();

	                        t.setMonth(e.getKey());
	                        t.setDrivingDirectionCount(
	                                list.stream().mapToLong(GMBLocationInsight::getActionsDrivingDirections).sum()
	                        );
	                        t.setCalls(
	                                list.stream().mapToLong(GMBLocationInsight::getActionsPhone).sum()
	                        );
	                        t.setWebsiteClicks(
	                                list.stream().mapToLong(GMBLocationInsight::getActionsWebsite).sum()
	                        );
	                        return t;
	                    })
	                    .toList();

	    /* ---------------- DAILY VIEWS ---------------- */
	    List<GMBLocationInsightDailyViewsTrend> dailyViewsTrend =
	            insights.stream()
	                    .collect(Collectors.groupingBy(
	                            GMBLocationInsight::getReportDate,
	                            Collectors.summingLong(this::totalViews)
	                    ))
	                    .entrySet().stream()
	                    .sorted(Map.Entry.comparingByKey())
	                    .map(e -> {
	                        GMBLocationInsightDailyViewsTrend d =
	                                new GMBLocationInsightDailyViewsTrend();
	                        d.setDate(e.getKey());
	                        d.setViewsCount(e.getValue());
	                        return d;
	                    })
	                    .toList();

	    /* ---------------- DAILY ACTIONS ---------------- */
	    List<GMBLocationInsightDailyActionsTrend> dailyActionsTrend =
	            insights.stream()
	                    .collect(Collectors.groupingBy(GMBLocationInsight::getReportDate))
	                    .entrySet().stream()
	                    .sorted(Map.Entry.comparingByKey())
	                    .map(e -> {
	                        List<GMBLocationInsight> list = e.getValue();
	                        GMBLocationInsightDailyActionsTrend d =
	                                new GMBLocationInsightDailyActionsTrend();

	                        d.setDate(e.getKey());
	                        d.setDrivingDirectionCount(
	                                list.stream().mapToLong(GMBLocationInsight::getActionsDrivingDirections).sum()
	                        );
	                        d.setCalls(
	                                list.stream().mapToLong(GMBLocationInsight::getActionsPhone).sum()
	                        );
	                        d.setWebsiteClicks(
	                                list.stream().mapToLong(GMBLocationInsight::getActionsWebsite).sum()
	                        );
	                        return d;
	                    })
	                    .toList();

	    /* ---------------- DEALER WISE ---------------- */
	    List<GMBLocationInsightDealerViewsActionsTrend> dealerViewsActionsTrend =
	            insights.stream()
	                    .filter(i -> i.getDealerId() != null)
	                    .collect(Collectors.groupingBy(GMBLocationInsight::getDealerId))
	                    .entrySet().stream()
	                    .map(e -> {
	                        String dealerId = e.getKey();
	                        DealerLocation dealer =
	                                dealerIdVsLocationMap.get(dealerId);

	                        List<GMBLocationInsight> list = e.getValue();

	                        GMBLocationInsightDealerViewsActionsTrend t =
	                                new GMBLocationInsightDealerViewsActionsTrend();

	                        t.setDealerId(dealerId);

	                        if (dealer != null) {
	                            t.setDealerName(dealer.getDealerName());
	                            t.setState(dealer.getState());
	                            t.setCity(dealer.getCity());
	                        }

	                        t.setViewsCount(
	                                list.stream().mapToLong(this::totalViews).sum()
	                        );
	                        t.setDrivingDirectionCount(
	                                list.stream().mapToLong(GMBLocationInsight::getActionsDrivingDirections).sum()
	                        );
	                        t.setCalls(
	                                list.stream().mapToLong(GMBLocationInsight::getActionsPhone).sum()
	                        );
	                        t.setWebsiteClicks(
	                                list.stream().mapToLong(GMBLocationInsight::getActionsWebsite).sum()
	                        );

	                        return t;
	                    })
	                    .toList();

	    response.setMonthlyViews(monthlyViews);
	    response.setMonthlyActionsTrend(monthlyActionsTrend);
	    response.setDailyViewsTrend(dailyViewsTrend);
	    response.setDailyActionsTrend(dailyActionsTrend);
	    response.setDealerViewsActionsTrend(dealerViewsActionsTrend);

	    return response;
	}
	private long totalViews(GMBLocationInsight i) {
	    return i.getBusinessImpressionsDesktopMaps()
	         + i.getBusinessImpressionsMoblieMaps()
	         + i.getBusinessImpressionsDesktopSearch()
	         + i.getBusinessImpressionsMoblieSearch();
	}

	private LocalDate toLocalDate(Date date) {
	    return new java.sql.Date(date.getTime()).toLocalDate();
	}

	public List<GMBLocationInsight> findByClientIdAndFromDate(String clientId, Set<String> mappedDealers,
			Date fromDate, Date toDate) {
		if (mappedDealers == null || mappedDealers.isEmpty()) {
	        return Collections.emptyList(); // avoids Hibernate IN () issue
	    }
		return gmbLocationInsightRepository.findByClientIdAndDealerIdInAndReportDateBetween(clientId, mappedDealers, fromDate, toDate);
		 
	}


}
