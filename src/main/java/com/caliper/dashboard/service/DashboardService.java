package com.caliper.dashboard.service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.campaign.google.entity.GoogleCampaign;
import com.caliper.campaign.google.service.CampaignService;
import com.caliper.dashboard.dto.DashboardRequestDTO;
import com.caliper.dashboard.dto.campaign.CampaignSummary;
import com.caliper.dashboard.dto.campaign.DashboardCampaign;
import com.caliper.dashboard.dto.campaign.MonthlySpend;
import com.caliper.dashboard.dto.insights.DashboardGoogleInsights;
import com.caliper.dashboard.dto.insights.InsightSummary;
import com.caliper.dashboard.dto.insights.MapSearchTrend;
import com.caliper.dashboard.dto.location.DashboardLocations;
import com.caliper.dashboard.dto.post.DashboardPosts;
import com.caliper.dashboard.dto.post.PostLocation;
import com.caliper.dashboard.dto.post.PostsGraphData;
import com.caliper.dashboard.dto.post.PostsSummary;
import com.caliper.dashboard.dto.review.AggregatedReviewDTO;
import com.caliper.dashboard.dto.review.DashboardReviews;
import com.caliper.dashboard.dto.review.PieData;
import com.caliper.dashboard.dto.review.RatingsBreakdown;
import com.caliper.dashboard.dto.review.ReviewLocation;
import com.caliper.dashboard.dto.review.ReviewSentiment;
import com.caliper.dashboard.dto.review.ReviewSummary;
import com.caliper.dashboard.dto.review.ReviewsChart;
import com.caliper.dashboard.dto.socialMediaInsights.DashboardSocialMediaInsights;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.facebook.service.FacebookLocationService;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.service.GBPCompletenessScoreCalculator;
import com.caliper.location.gmb.service.GMBLocationService;
import com.caliper.location.service.DealerLocationService;
import com.caliper.location.service.LocationService;
import com.caliper.planmanagement.entity.Plan;
import com.caliper.planmanagement.repository.PlanRepository;
import com.caliper.post.entity.Platform;
import com.caliper.post.entity.Post;
import com.caliper.post.entity.PostLocationMap;
import com.caliper.post.service.PostService;
import com.caliper.reporting.entity.GMBLocationInsight;
import com.caliper.reporting.service.GMBInsightService;
import com.caliper.review.service.ReviewService;
import com.caliper.usermanagement.entity.Roles;
import com.caliper.usermanagement.entity.UserRoleClientMapping;
import com.caliper.usermanagement.service.UserService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class DashboardService {

	@Autowired
	private DealerLocationService dealerLocationService;

	@Autowired
	private GMBLocationService gmbLocationService;

	@Autowired
	private GBPCompletenessScoreCalculator gbpCompletenessScoreCalculator;

	@Autowired 
	private FacebookLocationService facebookLocationService;

	@Autowired
	private CampaignService campaignService;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private LocationService locationService;
	
	@Autowired
	private GMBInsightService gmbInsightService;

	@Autowired
	private PostService postService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private PlanRepository planRepository;

	Calendar calendar = Calendar.getInstance();

	public boolean hasAccess(DashboardRequestDTO requestDTO, String module) {

		List<UserRoleClientMapping> userClientLocMappings = userService.getUserRoleClientMappingByUserIdAndClientId(requestDTO.getUserId(), requestDTO.getClientId());

		for (UserRoleClientMapping mapping : userClientLocMappings) {

			if (mapping.getRole().equalsIgnoreCase(module) || mapping.getRole().equalsIgnoreCase((Roles.SUPER_ADMIN).name()) 
					|| mapping.getRole().equalsIgnoreCase((Roles.ADMIN).name())) {
				return true; 
			}
		}

		return false;
	}

	public Date getFromDate(Date fromDate) {
		if (fromDate == null) {  // checks if no value is passed
			fromDate = DateUtils.addDays(new Date(), -364);
		}
		return fromDate;
	}
	
	public Date getToDate(Date toDate) {

		if (toDate == null) {
			toDate = DateUtils.addMilliseconds(DateUtils.ceiling(new Date(), Calendar.DAY_OF_MONTH), -1);
		}
		return toDate;
	}

	public DashboardLocations fetchDashboardLocationDetails(DashboardRequestDTO requestDTO) {

		String clientId = requestDTO.getClientId();

		Set<String> mappedDealers = locationService.getLocationsMappedToUser(requestDTO.getClientId(), requestDTO.getUserId());

		Plan plan = planRepository.findByClientId(clientId);
	
		//Long locationCount = dealerLocationService.fetchLocationsCount(clientId, mappedDealers);
		Long locationCount = plan.getLocationCount();
		
		Long gmbCount = gmbLocationService.fetchLocationsCount(clientId, mappedDealers);
		Long facebookCount = facebookLocationService.fetchLocationsCount(clientId, mappedDealers);

		Long auditScore = calculateAuditScore(clientId, mappedDealers);

		DashboardLocations dashboardLocations = new DashboardLocations(locationCount, gmbCount, facebookCount, 0L, auditScore);
		return dashboardLocations;
	}

	/**
	 * Audit score = average GBP profile-completeness score (0-100, 19-param rubric),
	 * across every GMB location mapped to this user for this client.
	 */
	private Long calculateAuditScore(String clientId, Set<String> mappedDealers) {

		if (mappedDealers.isEmpty()) {
			return 0L;
		}

		List<GMBLocation> gmbLocations =
				gmbLocationService.fetchLocationsByClientAndDealerIds(clientId, mappedDealers);

		if (gmbLocations.isEmpty()) {
			return 0L;
		}

		double averageScore = gmbLocations.stream()
				.mapToInt(gbpCompletenessScoreCalculator::calculateScore)
				.average()
				.orElse(0.0);

		return Math.round(averageScore);
	}

	public DashboardCampaign fetchDashboardCampaignDetails(DashboardRequestDTO requestDTO) {
		
	    try {
	        String clientId = requestDTO.getClientId();

	        // Get dealers mapped to user
	        Set<String> mappedDealers = locationService.getLocationsMappedToUser(clientId, requestDTO.getUserId());

	        // Handle dates
	        Date fromDate = getFromDate(requestDTO.getFromDate());
	        Date toDate = getToDate(requestDTO.getToDate()); // will now include full day

	        // Fetch campaigns
	        List<GoogleCampaign> googleCampaigns = Optional.ofNullable(
	                campaignService.findByClientIdAndDealerIdInAndStartDateBetween(
	                        clientId, mappedDealers, fromDate, toDate
	                )
	        ).orElse(Collections.emptyList());

	        // Total spend
	        BigDecimal totalSpend = googleCampaigns.stream()
	                .map(c -> Optional.ofNullable(c.getTotalBudget()).orElse(BigDecimal.ZERO))
	                .reduce(BigDecimal.ZERO, BigDecimal::add);

	        // Total campaigns
	        long totalCampaigns = googleCampaigns.size();

	        // Active dealers in last 3 months
	        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
	        long activeDealers = googleCampaigns.stream()
	                .filter(c -> {
	                    LocalDate startDate = Instant.ofEpochMilli(c.getStartDate().getTime())
	                            .atZone(ZoneId.systemDefault())
	                            .toLocalDate();
	                    return !startDate.isBefore(threeMonthsAgo);
	                })
	                .map(GoogleCampaign::getDealerId)
	                .distinct()
	                .count();

	        // Spend grouped by month
	        Map<YearMonth, BigDecimal> spendByYearMonth = googleCampaigns.stream()
	                .collect(Collectors.groupingBy(
	                        c -> YearMonth.from(
	                                Instant.ofEpochMilli(c.getStartDate().getTime())
	                                        .atZone(ZoneId.systemDefault())
	                                        .toLocalDate()
	                        ),
	                        Collectors.reducing(
	                                BigDecimal.ZERO,
	                                c -> Optional.ofNullable(c.getTotalBudget()).orElse(BigDecimal.ZERO),
	                                BigDecimal::add
	                        )
	                ));

	        YearMonth start = YearMonth.from(fromDate.toInstant()
	                .atZone(ZoneId.systemDefault())
	                .toLocalDate());

	        YearMonth end = YearMonth.from(toDate.toInstant()
	                .atZone(ZoneId.systemDefault())
	                .toLocalDate());

	        List<MonthlySpend> spends = new ArrayList<>();

	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

	        while (!start.isAfter(end)) {
	            spends.add(new MonthlySpend(
	                    start.format(formatter), // Example: Jan 2026
	                    spendByYearMonth.getOrDefault(start, BigDecimal.ZERO)
	            ));
	            start = start.plusMonths(1);
	        }
	        // Campaign summary
	        CampaignSummary campaignSummary = new CampaignSummary(totalSpend, 0.0, totalCampaigns, activeDealers);

	        return new DashboardCampaign(campaignSummary, spends);

	    } catch (Exception e) {
	        log.error("Failed to fetch dashboard campaign details", e);
	        throw new RuntimeException("Failed to fetch dashboard campaign details", e);
	    }
		/*
		  // 1️⃣ Campaign Summary
	    CampaignSummary summary = new CampaignSummary(
	            new BigDecimal("125000.50"),  // totalSpends
	            2450000.0,                    // impressions
	            32L,                          // totalCampaigns
	            18L                           // activeCampaigns
	    );

	    // 2️⃣ Monthly Spend Trends
	    List<MonthlySpend> spends = new ArrayList<>();

	    spends.add(new MonthlySpend("Jan 2026", new BigDecimal("8500.00")));
	    spends.add(new MonthlySpend("Feb 2026", new BigDecimal("9200.00")));
	    spends.add(new MonthlySpend("Mar 2025", new BigDecimal("10150.00")));
	    spends.add(new MonthlySpend("Apr 2025", new BigDecimal("11000.00")));
	    spends.add(new MonthlySpend("May 2025", new BigDecimal("12500.00")));
	    spends.add(new MonthlySpend("Jun 2025", new BigDecimal("13800.00")));
	    spends.add(new MonthlySpend("Jul 2025", new BigDecimal("14200.00")));
	    spends.add(new MonthlySpend("Aug 2025", new BigDecimal("13900.00")));
	    spends.add(new MonthlySpend("Sep 2025", new BigDecimal("12800.00")));
	    spends.add(new MonthlySpend("Oct 2025", new BigDecimal("11750.00")));
	    spends.add(new MonthlySpend("Nov 2025", new BigDecimal("9900.00")));
	    spends.add(new MonthlySpend("Dec 2025", new BigDecimal("8700.00")));


	    // 3️⃣ Final Response
	    return new DashboardCampaign(summary, spends);*/
	}

	public DashboardReviews fetchDashboardReviewDetails(DashboardRequestDTO requestDTO) {
	    try {
	        String clientId = requestDTO.getClientId();

	        // 1️⃣ Get dealer IDs mapped to the user
	        Set<String> mappedDealers = locationService.getLocationsMappedToUser(clientId, requestDTO.getUserId());
	        if (mappedDealers.isEmpty()) {
	            return new DashboardReviews();
	        }

	        // 2️⃣ Handle date range
	        Date fromDate = getFromDate(requestDTO.getFromDate());
	        Date toDate = getToDate(requestDTO.getToDate());

	        // 3️⃣ Fetch dealer locations
	        List<DealerLocation> dealerLocations = dealerLocationService.findByDealerIdInAndClientId(mappedDealers, clientId);
	        Map<String, String> dealerIdToNameMap = dealerLocations.stream()
	                .collect(Collectors.toMap(DealerLocation::getDealerId, DealerLocation::getDealerName));

	        // 4️⃣ Fetch aggregated reviews
	        List<AggregatedReviewDTO> aggregatedReviews = reviewService.fetchAggregatedReviews(
	                clientId, mappedDealers, fromDate, toDate
	        );

	        // 5️⃣ Initialize counters
	        long totalReviews = 0;
	        long ratingSum = 0;
	        double positive = 0, neutral = 0, negative = 0;
	        double five = 0, four = 0, three = 0, two = 0, one = 0;

	        Map<String, List<AggregatedReviewDTO>> reviewsByLocation = new HashMap<>();

	        // 6️⃣ Aggregate reviews by YearMonth
	        Map<YearMonth, Long> reviewsByYearMonth = new HashMap<>();
	        Map<YearMonth, Long> ratingSumByYearMonth = new HashMap<>();

	        for (AggregatedReviewDTO ar : aggregatedReviews) {
	            String dealerId = ar.getDealerId();
	            long count = ar.getTotalReviews();

	            totalReviews += count;
	            ratingSum += ar.getRatingSum();

	            positive += ar.getPositive();
	            neutral += ar.getNeutral();
	            negative += ar.getNegative();

	            five += ar.getFive();
	            four += ar.getFour();
	            three += ar.getThree();
	            two += ar.getTwo();
	            one += ar.getOne();

	            reviewsByLocation.computeIfAbsent(dealerId, k -> new ArrayList<>()).add(ar);

	            YearMonth yearMonth = YearMonth.of(ar.getYear(), ar.getMonth());
	            reviewsByYearMonth.merge(yearMonth, count, Long::sum);
	            ratingSumByYearMonth.merge(yearMonth, ar.getRatingSum(), Long::sum);
	        }

	        // 7️⃣ Overall average rating
	        double avgRating = totalReviews == 0 ? 0.0 : (double) ratingSum / totalReviews;
	        double roundedAvgRating = round(avgRating, 1);

	        // 8️⃣ Sentiment percentages
	        ReviewSentiment sentiment = new ReviewSentiment(List.of(
	                new PieData("Positive", totalReviews == 0 ? 0 : (int) Math.round((positive * 100.0) / totalReviews)),
	                new PieData("Negative", totalReviews == 0 ? 0 : (int) Math.round((negative * 100.0) / totalReviews)),
	                new PieData("Neutral", totalReviews == 0 ? 0 : (int) Math.round((neutral * 100.0) / totalReviews))
	        ));

	        // 9️⃣ Ratings breakdown percentages
	        RatingsBreakdown breakdown = new RatingsBreakdown(
	                totalReviews == 0 ? 0 : (int) Math.round((five / totalReviews) * 100),
	                totalReviews == 0 ? 0 : (int) Math.round((four / totalReviews) * 100),
	                totalReviews == 0 ? 0 : (int) Math.round((three / totalReviews) * 100),
	                totalReviews == 0 ? 0 : (int) Math.round((two / totalReviews) * 100),
	                totalReviews == 0 ? 0 : (int) Math.round((one / totalReviews) * 100)
	        );

	        // 10️⃣ Locations with weighted average rating
	        List<ReviewLocation> locations = reviewsByLocation.entrySet().stream()
	                .map(e -> {
	                    String dealerId = e.getKey();
	                    long totalRatingSum = e.getValue().stream().mapToLong(AggregatedReviewDTO::getRatingSum).sum();
	                    long totalReviewsForDealer = e.getValue().stream().mapToLong(AggregatedReviewDTO::getTotalReviews).sum();
	                    double avg = totalReviewsForDealer == 0 ? 0.0 : (double) totalRatingSum / totalReviewsForDealer;
	                    avg = round(avg, 1);
	                    String dealerName = dealerIdToNameMap.getOrDefault(dealerId, "Unknown");
	                    return new ReviewLocation(dealerId, dealerName, totalReviewsForDealer, avg);
	                })
	                .toList();

	        // 11️⃣ Top 5 locations
	        List<ReviewLocation> topLocations = locations.stream()
	                .sorted(
	                        Comparator.comparingLong(ReviewLocation::getRatings).reversed()
	                                .thenComparing(Comparator.comparingDouble(ReviewLocation::getAverageRating).reversed())
	                )
	                .limit(5)
	                .toList();

	        // 12️⃣ Lowest 5 locations
	        List<ReviewLocation> lowestLocations = locations.stream()
	                .sorted(
	                        Comparator.comparingDouble(ReviewLocation::getAverageRating)
	                                .thenComparingLong(ReviewLocation::getRatings)
	                )
	                .limit(5)
	                .toList();

	        // 13️⃣ Monthly reviews chart — now uses YearMonth
	        YearMonth startYM = YearMonth.from(fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	        YearMonth endYM = YearMonth.from(toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

	        List<ReviewsChart> reviewsChart = new ArrayList<>();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

	        while (!startYM.isAfter(endYM)) {
	            long monthReviews = reviewsByYearMonth.getOrDefault(startYM, 0L);
	            long monthRatingSum = ratingSumByYearMonth.getOrDefault(startYM, 0L);
	            double monthAvgRating = monthReviews == 0 ? 0.0 : round((double) monthRatingSum / monthReviews, 1);

	            reviewsChart.add(new ReviewsChart(startYM.format(formatter), monthReviews, monthAvgRating));
	            startYM = startYM.plusMonths(1);
	        }

	        // 14️⃣ NPS
	        double nps = totalReviews == 0 ? 0 : round(((positive - negative) / totalReviews) * 100, 1);
	        ReviewSummary reviewSummary = new ReviewSummary(totalReviews, roundedAvgRating, nps);

	        return new DashboardReviews(
	                reviewSummary,
	                sentiment,
	                breakdown,
	                topLocations,
	                lowestLocations,
	                reviewsChart
	        );

	    } catch (Exception e) {
	        log.error("Failed to fetch dashboard review details", e);
	        throw new RuntimeException("Failed to fetch dashboard review details", e);
	    }
	}
	    /*
		  ReviewSummary summary = new ReviewSummary(
		            5420L,     // totalReviews
		            4.3,       // avgRating
		            62.5       // nps
		    );

		    // 2️⃣ Review Sentiment (Pie Chart Data)
		    List<PieData> pieData = new ArrayList<>();
		    pieData.add(new PieData("Positive", 3800));
		    pieData.add(new PieData("Neutral", 950));
		    pieData.add(new PieData("Negative", 670));

		    ReviewSentiment sentiment = new ReviewSentiment(pieData);
		    RatingsBreakdown breakdown = new RatingsBreakdown(
		            31,  // fiveStar
		            12,  // fourStar
		            60,   // threeStar
		            32,   // twoStar
		            20   // oneStar
		    );

		    // 4️⃣ Top Review Locations
		    List<ReviewLocation> topLocations = new ArrayList<>();
		    topLocations.add(new ReviewLocation(101L, "New York Downtown", 540, 4.8));
		    topLocations.add(new ReviewLocation(102L, "Los Angeles Central", 480, 4.7));
		    topLocations.add(new ReviewLocation(103L, "Chicago West", 450, 4.6));
		    // 5️⃣ Lowest Review Locations
		    List<ReviewLocation> lowestLocations = new ArrayList<>();
		    lowestLocations.add(new ReviewLocation(201L, "Houston South", 310, 3.2));
		    lowestLocations.add(new ReviewLocation(202L, "Phoenix East", 290, 3.4));
		    lowestLocations.add(new ReviewLocation(203L, "Dallas North", 270, 3.5));

		    // 6️⃣ Monthly Review Chart
		    List<ReviewsChart> reviewsChart = new ArrayList<>();

		    reviewsChart.add(new ReviewsChart("Jan 2026", 420, 4.2));
		    reviewsChart.add(new ReviewsChart("Feb 2026", 460, 4.3));
		    reviewsChart.add(new ReviewsChart("Mar 2025", 510, 4.4));
		    reviewsChart.add(new ReviewsChart("Apr 2025", 530, 4.5));
		    reviewsChart.add(new ReviewsChart("May 2025", 580, 4.6));
		    reviewsChart.add(new ReviewsChart("Jun 2025", 610, 4.5));
		    reviewsChart.add(new ReviewsChart("Jul 2025", 640, 4.4));
		    reviewsChart.add(new ReviewsChart("Aug 2025", 620, 4.3));
		    reviewsChart.add(new ReviewsChart("Sep 2025", 590, 4.2));
		    reviewsChart.add(new ReviewsChart("Oct 2025", 560, 4.1));
		    reviewsChart.add(new ReviewsChart("Nov 2025", 500, 4.0));
		    reviewsChart.add(new ReviewsChart("Dec 2025", 470, 4.1));


		    // 7️⃣ Final Response
		    return new DashboardReviews(
		            summary,
		            sentiment,
		            breakdown,
		            topLocations,
		            lowestLocations,
		            reviewsChart
		    );*/

	private double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();
		long factor = (long) Math.pow(10, places);
		return Math.round(value * factor) / (double) factor;
	}

	public DashboardGoogleInsights fetchDashboardGoogleInsightDetails(DashboardRequestDTO requestDTO) {

	    String clientId = requestDTO.getClientId();
	    Set<String> mappedDealers = locationService.getLocationsMappedToUser(clientId, requestDTO.getUserId());
	    
	    // ===== Handle empty dealers =====
	    if (mappedDealers == null || mappedDealers.isEmpty()) {
	        InsightSummary emptySummary = new InsightSummary(0L, 0L, 0L, 0L, 0L, 0L);
	        return new DashboardGoogleInsights(emptySummary, Collections.emptyList());
	    }
	    
	    Date fromDate = getFromDate(requestDTO.getFromDate());
	    Date toDate = getToDate(requestDTO.getToDate());

	    List<GMBLocationInsight> gmbLocationInsights =
	            Optional.ofNullable(
	                    gmbInsightService.findByClientIdAndFromDate(clientId, mappedDealers, fromDate, toDate)
	            ).orElse(Collections.emptyList());

	    // ===== Summary totals =====
	    long totalSearches = gmbLocationInsights.stream()
	            .mapToLong(GMBLocationInsight::getViewsSearch)
	            .sum();
	    long totalMapViews = gmbLocationInsights.stream()
	            .mapToLong(GMBLocationInsight::getViewsMaps)
	            .sum();
	    
	    long totalSummaryViews = totalSearches + totalMapViews;
	    long callsInitiated = gmbLocationInsights.stream()
	            .mapToLong(GMBLocationInsight::getActionsPhone)
	            .sum();
	    long websiteClicks = gmbLocationInsights.stream()
	            .mapToLong(GMBLocationInsight::getActionsWebsite)
	            .sum();
	    long drivingDirectionReq = gmbLocationInsights.stream()
	            .mapToLong(GMBLocationInsight::getActionsDrivingDirections)
	            .sum();

	    InsightSummary summary = new InsightSummary(
	    		totalSummaryViews,
	            totalSearches,
	            totalMapViews,
	            callsInitiated,
	            websiteClicks,
	            drivingDirectionReq
	    );

	    // ===== Aggregate by YearMonth =====
	    Map<YearMonth, MapSearchTrend> trendsByYearMonth = new HashMap<>();

	    gmbLocationInsights.forEach(g -> {
	        LocalDate localDate = g.getReportDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	        YearMonth yearMonth = YearMonth.from(localDate);

	        long totalActions = g.getActionsPhone() + g.getActionsWebsite() + g.getActionsDrivingDirections();
	        long totalViews = g.getViewsMaps() + g.getViewsSearch();

	        trendsByYearMonth.merge(
	                yearMonth,
	                new MapSearchTrend(yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")), totalActions, totalViews),
	                (existing, newVal) -> {
	                    existing.setTotalActions(existing.getTotalActions() + newVal.getTotalActions());
	                    existing.setTotalViews(existing.getTotalViews() + newVal.getTotalViews());
	                    return existing;
	                }
	        );
	    });

	    // ===== Generate complete list from fromDate → toDate =====
	    YearMonth startYM = YearMonth.from(fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	    YearMonth endYM = YearMonth.from(toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

	    List<MapSearchTrend> mapSearchTrends = new ArrayList<>();
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

	    while (!startYM.isAfter(endYM)) {
	        MapSearchTrend trend = trendsByYearMonth.getOrDefault(
	                startYM,
	                new MapSearchTrend(startYM.format(formatter), 0L, 0L)
	        );
	        mapSearchTrends.add(trend);
	        startYM = startYM.plusMonths(1);
	    }

	    return new DashboardGoogleInsights(summary, mapSearchTrends);
		 /*
		InsightSummary summary = new InsightSummary(
				12850L,  // totalSearches
				9420L,   // totalMapViews
				615L,    // callsInitiated
				480L,    // websiteClicks
				710L     // drivingDirectionReq
				);

		// 2️⃣ Monthly map search trends with sample values
		List<MapSearchTrend> mapSearchTrends = new ArrayList<>();

		mapSearchTrends.add(new MapSearchTrend("Jan 2026", 820L, 640L));
		mapSearchTrends.add(new MapSearchTrend("Feb 2026", 910L, 700L));
		mapSearchTrends.add(new MapSearchTrend("Mar 2025", 1080L, 850L));
		mapSearchTrends.add(new MapSearchTrend("Apr 2025", 1150L, 900L));
		mapSearchTrends.add(new MapSearchTrend("May 2025", 1240L, 980L));
		mapSearchTrends.add(new MapSearchTrend("Jun 2025", 1320L, 1050L));
		mapSearchTrends.add(new MapSearchTrend("Jul 2025", 1400L, 1120L));
		mapSearchTrends.add(new MapSearchTrend("Aug 2025", 1380L, 1100L));
		mapSearchTrends.add(new MapSearchTrend("Sep 2025", 1290L, 1020L));
		mapSearchTrends.add(new MapSearchTrend("Oct 2025", 1180L, 950L));
		mapSearchTrends.add(new MapSearchTrend("Nov 2025", 1010L, 820L));
		mapSearchTrends.add(new MapSearchTrend("Dec 2025", 960L, 780L));


		// 3️⃣ Final response
		return new DashboardGoogleInsights(summary, mapSearchTrends);*/
	}

	public DashboardPosts fetchDashboardPostsDetails(DashboardRequestDTO requestDTO) {

	    String clientId = requestDTO.getClientId();
	    Set<String> mappedDealers = locationService.getLocationsMappedToUser(clientId, requestDTO.getUserId());
	    Date fromDate = getFromDate(requestDTO.getFromDate());
	    Date toDate = getToDate(requestDTO.getToDate());

	    List<DealerLocation> dealerLocations = dealerLocationService.findByDealerIdInAndClientId(mappedDealers, clientId);
	    Map<String, String> dealerIdToNameMap = dealerLocations.stream()
	            .collect(Collectors.toMap(DealerLocation::getDealerId, DealerLocation::getDealerName));

	    List<PostLocationMap> mappings = Optional.ofNullable(
	            postService.findByClientIdAndDealerIdInAndCreatedDateBetween(clientId, mappedDealers, fromDate, toDate)
	    ).orElse(Collections.emptyList());

	    Set<Long> postIds = mappings.stream()
	            .map(PostLocationMap::getPostId)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toSet());
	    // ===== Posts by location =====
	    Map<String, Long> postsByLocation = mappings.stream()
	            .collect(Collectors.groupingBy(PostLocationMap::getDealerId, Collectors.counting()));
	    
	    List<Post> postByPostIdsAndPlatform = postService.getPostByPostIdsAndPlatform(clientId, postIds);

	    List<PostLocation> locations = postsByLocation.entrySet().stream()
	            .map(e -> new PostLocation(
	                    e.getKey(),
	                    dealerIdToNameMap.getOrDefault(e.getKey(), "Unknown"),
	                    e.getValue()
	            ))
	            .toList();

	    // Top 5 locations
	    List<PostLocation> topLocations = locations.stream()
	            .sorted(Comparator.comparingDouble(PostLocation::getAveragePosts).reversed())
	            .limit(5)
	            .toList();

	    // Bottom 5 locations
	    List<PostLocation> leastLocations = locations.stream()
	            .sorted(Comparator.comparingDouble(PostLocation::getAveragePosts))
	            .limit(5)
	            .toList();

	    // ===== Group by YearMonth + Platform =====
	    Map<YearMonth, Map<Platform, Long>> postsByYearMonth = mappings.stream()
	    	    .collect(Collectors.groupingBy(
	    	        m -> YearMonth.from(
	    	            m.getCreatedDate().toInstant()
	    	             .atZone(ZoneId.systemDefault())
	    	             .toLocalDate()
	    	        ),
	    	        Collectors.groupingBy(
	    	            m -> m.getPost().getPlatform(),
	    	            Collectors.mapping(
	    	                PostLocationMap::getPostId,
	    	                Collectors.collectingAndThen(
	    	                    Collectors.toSet(),
	    	                    set -> (long) set.size()
	    	                )
	    	            )
	    	        )
	    	    ));


	    // ===== Generate full range from fromDate → toDate =====
	    YearMonth startYM = YearMonth.from(fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	    YearMonth endYM = YearMonth.from(toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

	    List<PostsGraphData> graphData = new ArrayList<>();
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

	    while (!startYM.isAfter(endYM)) {
	        Map<Platform, Long> platformCounts = postsByYearMonth.getOrDefault(startYM, Collections.emptyMap());

	        graphData.add(new PostsGraphData(
	                startYM.format(formatter),
	                platformCounts.getOrDefault(Platform.GMB, 0L),
	                platformCounts.getOrDefault(Platform.FACEBOOK, 0L)
	        ));
	        startYM = startYM.plusMonths(1);
	    }

	    // ===== Summary =====
	    long totalPosts = mappings.stream()
	            .map(PostLocationMap::getPostId)
	            .distinct()
	            .count();

	    long pendingPosts = mappings.stream()
	            .filter(m -> Post.STATUS_SUBMIT.equals(m.getPost().getStatus()))
	            .map(PostLocationMap::getPostId)
	            .distinct()
	            .count();

	    PostsSummary postsSummary = new PostsSummary(totalPosts, pendingPosts);

	    return new DashboardPosts(postsSummary, topLocations, leastLocations, graphData);

		 /*
		PostsSummary summary = new PostsSummary(
				124L,   // totalPosts
				18L     // unDeployedPosts
				);

		// 2️⃣ Top post locations
		List<PostLocation> topPostLocations = new ArrayList<>();
		topPostLocations.add(new PostLocation(1L, "Mumbai", 66));
		topPostLocations.add(new PostLocation(2L, "Delhi", 58));
		topPostLocations.add(new PostLocation(3L, "Bengaluru", 45));
		topPostLocations.add(new PostLocation(4L, "Pune",39));
		topPostLocations.add(new PostLocation(5L, "Chennai", 24));

		// 3️⃣ Least post locations
		List<PostLocation> leastPostLocations = new ArrayList<>();
		leastPostLocations.add(new PostLocation(21L, "Agra", 1));
		leastPostLocations.add(new PostLocation(22L, "Jodhpur", 3));
		leastPostLocations.add(new PostLocation(23L, "Udaipur", 6));
		leastPostLocations.add(new PostLocation(24L, "Bikaner", 7));
		leastPostLocations.add(new PostLocation(25L, "Ajmer",9));

		// 4️⃣ Posts graph data (monthly)
		List<PostsGraphData> postsGraphData = new ArrayList<>();

		postsGraphData.add(new PostsGraphData("Jan 2026", 32L, 18L));
		postsGraphData.add(new PostsGraphData("Feb 2026", 28L, 15L));
		postsGraphData.add(new PostsGraphData("Mar 2025", 36L, 21L));
		postsGraphData.add(new PostsGraphData("Apr 2025", 41L, 26L));
		postsGraphData.add(new PostsGraphData("May 2025", 44L, 29L));
		postsGraphData.add(new PostsGraphData("Jun 2025", 39L, 24L));
		postsGraphData.add(new PostsGraphData("Jul 2025", 46L, 31L));
		postsGraphData.add(new PostsGraphData("Aug 2025", 48L, 34L));
		postsGraphData.add(new PostsGraphData("Sep 2025", 42L, 28L));
		postsGraphData.add(new PostsGraphData("Oct 2025", 38L, 25L));
		postsGraphData.add(new PostsGraphData("Nov 2025", 34L, 22L));
		postsGraphData.add(new PostsGraphData("Dec 2025", 29L, 19L));


		// 5️⃣ Final response
		return new DashboardPosts(
				summary,
				topPostLocations,
				leastPostLocations,
				postsGraphData
				);*/
	}

	public DashboardSocialMediaInsights fetchDashboardSocialMediaInsightsDetails(DashboardRequestDTO requestDTO) {

		DashboardSocialMediaInsights dashboardSocialMediaInsights = new DashboardSocialMediaInsights(0L, 0L, 0L);
		return dashboardSocialMediaInsights;
	}
}
