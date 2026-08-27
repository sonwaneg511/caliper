package com.caliper.reporting.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.campaign.google.entity.ClientLocationSetup;
import com.caliper.campaign.google.repository.ClientLocationSetupRepository;
import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.gmb.dto.response.GMBOperationHoursResponse;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.entity.GMBOperationHours;
import com.caliper.location.gmb.repository.GMBLocationRepository;
import com.caliper.location.gmb.service.GBPCompletenessScoreCalculator;
import com.caliper.location.repository.DealerLocationRepository;
import com.caliper.location.service.DealerLocationService;
import com.caliper.post.Specification.PostSpecification;
import com.caliper.post.Specification.PostLocationMapSpecification.PostLocationMapSpecification;
import com.caliper.post.dto.Request.PostRequest;
import com.caliper.post.entity.Platform;
import com.caliper.post.entity.Post;
import com.caliper.post.entity.PostLocationMap;
import com.caliper.post.repository.PostLocationMapRepository;
import com.caliper.post.repository.PostRepository;
import com.caliper.reporting.dto.request.GMBLocationReportRequest;
import com.caliper.reporting.dto.request.InsightRequest;
import com.caliper.reporting.dto.response.ChartData;
import com.caliper.reporting.dto.response.CommentSplitReview;
import com.caliper.reporting.dto.response.GMBInsightLocationResponse;
import com.caliper.reporting.dto.response.GMBInsightLocationSumDataResponse;
import com.caliper.reporting.dto.response.GMBLocationReportPageResponse;
import com.caliper.reporting.dto.response.GMBLocationReportResponse;
import com.caliper.reporting.dto.response.GraphData;
import com.caliper.reporting.dto.response.InsightGraphResponse;
import com.caliper.reporting.dto.response.InsightReportingTableDataResponse;
import com.caliper.reporting.dto.response.PieData;
import com.caliper.reporting.dto.response.PostReportingData;
import com.caliper.reporting.dto.response.PostReportingDataPageResponse;
import com.caliper.reporting.dto.response.PostReportingResponse;
import com.caliper.reporting.dto.response.PostsGraphReportingData;
import com.caliper.reporting.dto.response.RatingCountData;
import com.caliper.reporting.dto.response.ReportingPostTableData;
import com.caliper.reporting.dto.response.ReviewGraphData;
import com.caliper.reporting.dto.response.ReviewOverViewData;
import com.caliper.reporting.dto.response.ReviewReportingDataPageResponse;
import com.caliper.reporting.dto.response.ReviewTableData;
import com.caliper.reporting.dto.response.ReviewTableRowData;
import com.caliper.reporting.dto.response.SentimentReview;
import com.caliper.reporting.entity.GMBLocationInsight;
import com.caliper.reporting.repository.GMBLocationInsightRepository;
import com.caliper.reporting.specification.GMBInsightSpecification;
import com.caliper.review.dto.request.ReviewRequest;
import com.caliper.review.entity.GMBReview;
import com.caliper.review.repository.GMBReviewRepository;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ReviewNotFoundException;

@Service
public class ReportingService {

    @Autowired
    private DealerLocationService dealerLocationService;

    @Autowired
    private DealerLocationRepository dealerLocationRepository;

    @Autowired
    private PostLocationMapRepository postLocationMapRepository;
    
    @Autowired
    private GMBLocationInsightRepository gmbLocationInsightRepository;

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private GMBReviewRepository gmbReviewRepository;

    @Autowired
    private GMBLocationRepository gmbLocationRepository;

    @Autowired
    private ClientLocationSetupRepository clientLocationSetupRepository;

    private static final int PAGE_SIZE = 10;

    @Autowired
    private GBPCompletenessScoreCalculator gbpCompletenessScoreCalculator;

    public LocationFilterRequest locationFilterRequestCreate(PostRequest req) {

        return LocationFilterRequest.builder()
                .clientId(req.getClientId())
                .userId(req.getUserId())
                .state(req.getState())
                .city(req.getCity())
                .dealerId(req.getDealerId())
                .build();
    }
    
    public LocationFilterRequest locationFilterRequestCreateReview(ReviewRequest req) {

        return LocationFilterRequest.builder()
                .clientId(req.getClientId())
                .userId(req.getUserId())
                .state(req.getState())
                .city(req.getCity())
                .dealerId(req.getDealerId())
                .build();
    }

//    public List<Post> getFilteredPost(PostRequest req) {
//
//        LocationFilterRequest locationRequest = locationFilterRequestCreate(req);
//
//        List<DealerLocation> locations =
//                dealerLocationService.getFilteredDealerLocation(locationRequest);
//
//        List<String> dealerIds =
//                locations.stream().map(DealerLocation::getDealerId).toList();
//
//        List<PostLocationMap> filterPostLocationMap =
//                postLocationMapRepository.findAll(
//                        PostLocationMapSpecification.filterPostLocationMap(req, dealerIds));
//
//        Set<Long> postIds =
//                filterPostLocationMap.stream().map(PostLocationMap::getPostId).collect(Collectors.toSet());
//
//        return postRepository.findAll(PostSpecification.filterPost(req, postIds));
//    }

    public PostReportingDataPageResponse getPostReportingData(PostRequest request) {

    	LocationFilterRequest locationRequest = locationFilterRequestCreate(request);

        List<DealerLocation> locations =
                dealerLocationService.getFilteredDealerLocation(locationRequest);

        List<String> dealerIds =
                locations.stream().map(DealerLocation::getDealerId).toList();

        List<PostLocationMap> filterPostLocationMap =
                postLocationMapRepository.findAll(
                        PostLocationMapSpecification.filterPostLocationMap(request, dealerIds));

        Set<Long> postIds =
                filterPostLocationMap.stream().map(PostLocationMap::getPostId).collect(Collectors.toSet());
        
        List<Post> filteredPosts = postRepository.findAll(PostSpecification.filterPost(request, postIds));
        filteredPosts.sort(Comparator.comparing(Post::getPostId).reversed());

        //-------------------------------------------------------
        // TOTAL POSTS
        //-------------------------------------------------------

        long totalPosts = filterPostLocationMap.size();

        long pendingDeployment = filterPostLocationMap.stream()
                .filter(post -> Post.STATUS_SUBMIT.equalsIgnoreCase(post.getStatus()))
                .count();

        //-------------------------------------------------------
        // PAGINATION
        //-------------------------------------------------------

        int pageNo = request.getPageNo();

        int start = pageNo * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filteredPosts.size());

        List<Post> paginatedPosts =
                start >= filteredPosts.size() ? new ArrayList<>() : filteredPosts.subList(start, end);

        //-------------------------------------------------------
        // LOCATIONS PER POST
        //-------------------------------------------------------

        List<Long> paginatedPostIds =
                paginatedPosts.stream().map(Post::getPostId).toList();

        List<PostLocationMap> allLocationMaps =
                postLocationMapRepository.getPostLocationMapByPostIds(paginatedPostIds);

        Map<Long, List<PostLocationMap>> locationMapsByPostId =
                allLocationMaps.stream()
                        .collect(Collectors.groupingBy(PostLocationMap::getPostId));

        //-------------------------------------------------------
        // TABLE DATA
        //-------------------------------------------------------

        List<PostReportingData> postReportingDataList = paginatedPosts.stream()
                .flatMap(post -> {
                    List<PostLocationMap> postLocations =
                            locationMapsByPostId.getOrDefault(post.getPostId(), List.of());

                    if (postLocations.isEmpty()) {
                        return Stream.of(PostReportingData.builder()
                                .date(post.getCreatedDate())
                                .title(post.getOfferTitle())
                                .postLabel(post.getSummary())
                                .createdBy(post.getCreatedBy())
                                .platform(post.getPlatform().name())
                                .postType(post.getPostType())
                                .postId(post.getPostId())
                                .clientId(post.getClientId())
                                .summary(post.getSummary())
                                .startDate(post.getStartDate())
                                .endDate(post.getEndDate())
                                .imageUrl(post.getImageUrl())
                                .mediaFormat(post.getMediaFormat())
                                .actionType(post.getActionType())
                                .actionUrl(post.getActionUrl())
                                .couponCode(post.getCouponCode())
                                .redeemUrl(post.getRedeemUrl())
                                .termsConditions(post.getTermsConditions())
                                .comment(post.getComment())
                                .status(post.getStatus())
                                .build());
                    }

                    return postLocations.stream()
                            .map(location -> PostReportingData.builder()
                                    .date(post.getCreatedDate())
                                    .title(post.getOfferTitle())
                                    .postLabel(post.getSummary())
                                    .createdBy(post.getCreatedBy())
                                    .platform(post.getPlatform().name())
                                    .postType(post.getPostType())
                                    .postId(post.getPostId())
                                    .dealerId(location.getDealerId())
                                    .clientId(post.getClientId())
                                    .summary(post.getSummary())
                                    .startDate(post.getStartDate())
                                    .endDate(post.getEndDate())
                                    .imageUrl(post.getImageUrl())
                                    .mediaFormat(post.getMediaFormat())
                                    .actionType(post.getActionType())
                                    .actionUrl(post.getActionUrl())
                                    .couponCode(post.getCouponCode())
                                    .redeemUrl(post.getRedeemUrl())
                                    .termsConditions(post.getTermsConditions())
                                    .comment(post.getComment())
                                    .status(location.getStatus())
                                    .build());
                })
                .toList();

        int totalPages = (int) Math.ceil((double) totalPosts / PAGE_SIZE);

        ReportingPostTableData reportingPostTableData =
                ReportingPostTableData.builder()
                        .postData(postReportingDataList)
                        .totalNoOfPages(totalPages)
                        .totalNoOfRecords(totalPosts)
                        .build();

        //-------------------------------------------------------
        // GRAPH DATA
        //-------------------------------------------------------

        Map<Date, List<Post>> postsGroupedByDate =
        		filteredPosts.stream().collect(Collectors.groupingBy(Post::getCreatedDate));

        List<PostsGraphReportingData> graphDataList = new ArrayList<>();

        for (Map.Entry<Date, List<Post>> entry : postsGroupedByDate.entrySet()) {

        	Date date = entry.getKey();
            List<Post> postList = entry.getValue();

            long totalGmbPosts =
                    postList.stream()
                            .filter(p -> p.getPlatform() == Platform.GMB)
                            .count();

            long totalFbPosts =
                    postList.stream()
                            .filter(p -> p.getPlatform() == Platform.FACEBOOK)
                            .count();

            graphDataList.add(
                    PostsGraphReportingData.builder()
                            .date(date)
                            .totalGMBPosts(totalGmbPosts)
                            .totalFbPosts(totalFbPosts)
                            .build());
        }


        //-------------------------------------------------------
        // FINAL RESPONSE
        //-------------------------------------------------------

        PostReportingResponse postReportingResponse =
                PostReportingResponse.builder()
                        .totalPosts(totalPosts)
                        .pendingPosts(pendingDeployment)
                        .postGraphReportingData(graphDataList)
                        .postTableData(reportingPostTableData)
                        .build();

        return PostReportingDataPageResponse.builder()
                .postDataResponseList(postReportingResponse)
                .build();
    }
    
    
    public LocationFilterRequest locationFilterRequestCreate(
            InsightRequest req) {

        return LocationFilterRequest.builder()
                .clientId(req.getClientId())
                .userId(req.getUserId())
                .state(req.getState())
                .city(req.getCity())
                .country(req.getCountry())
                .dealerId(req.getDealerId())
                .build();
    }


    /**
     * Fetch filtered insight data
     */
    public List<GMBLocationInsight> getFilteredInsight(
            InsightRequest req,
            Set<String> dealerIds) {

        return gmbLocationInsightRepository.findAll(
                GMBInsightSpecification.filterGMBLocationInsight(
                        req,
                        dealerIds
                )
        );
    }


    /**
     * Common utility method for sum
     */
    private long sumField(
            List<GMBLocationInsight> insights,
            Function<GMBLocationInsight, Long> mapper) {

        return insights.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
    }


    /**
     * Common utility method for fetching locations
     */
    private List<DealerLocation> getDealerLocations(
            InsightRequest request) {

        LocationFilterRequest locationRequest =
                locationFilterRequestCreate(request);

        return dealerLocationService
                .getFilteredDealerLocation(locationRequest);
    }


    /**
     * Common utility method for dealerIds
     */
    private Set<String> getDealerIds(
            List<DealerLocation> locations) {

        return locations.stream()
                .map(DealerLocation::getDealerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }


    /**
     * SUM API
     */
    public GMBInsightLocationSumDataResponse getInsightSumData(
            InsightRequest request) {

        // =====================================================
        // STEP 1 : FETCH LOCATIONS
        // =====================================================

        List<DealerLocation> locations =
                getDealerLocations(request);

        // =====================================================
        // STEP 2 : CREATE DEALER IDS
        // =====================================================

        Set<String> dealerIds =
                getDealerIds(locations);

        // =====================================================
        // STEP 3 : EMPTY RESPONSE
        // =====================================================

        GMBInsightLocationSumDataResponse response =
                new GMBInsightLocationSumDataResponse();

        if (dealerIds.isEmpty()) {
            return response;
        }

        // =====================================================
        // STEP 4 : FETCH INSIGHT DATA
        // =====================================================

        List<GMBLocationInsight> insightList =
                getFilteredInsight(request, dealerIds);

        // =====================================================
        // STEP 5 : MAP VIEWS
        // =====================================================

        long desktopMapViews =
                sumField(
                        insightList,
                        GMBLocationInsight::
                                getBusinessImpressionsDesktopMaps
                );

        long mobileMapViews =
                sumField(
                        insightList,
                        GMBLocationInsight::
                                getBusinessImpressionsMoblieMaps
                );

        response.setDesktopMapViews(
                desktopMapViews
        );

        response.setMobileMapViews(
                mobileMapViews
        );

        response.setTotalMapViews(
                desktopMapViews + mobileMapViews
        );

        // =====================================================
        // STEP 6 : SEARCH VIEWS
        // =====================================================

        long desktopSearchViews =
                sumField(
                        insightList,
                        GMBLocationInsight::
                                getBusinessImpressionsDesktopSearch
                );

        long mobileSearchViews =
                sumField(
                        insightList,
                        GMBLocationInsight::
                                getBusinessImpressionsMoblieSearch
                );

        response.setDesktopSearchViews(
                desktopSearchViews
        );

        response.setMobileSearchViews(
                mobileSearchViews
        );

        response.setTotalSearchViews(
                desktopSearchViews + mobileSearchViews
        );

        // =====================================================
        // STEP 7 : ACTIONS
        // =====================================================

        long drivingDirectionActions =
                sumField(
                        insightList,
                        GMBLocationInsight::
                                getActionsDrivingDirections
                );

        long websiteActions =
                sumField(
                        insightList,
                        GMBLocationInsight::
                                getActionsWebsite
                );

        long phoneActions =
                sumField(
                        insightList,
                        GMBLocationInsight::
                                getActionsPhone
                );

        response.setTotalDrivingDirectionActions(
                drivingDirectionActions
        );

        response.setTotalWebsiteActions(
                websiteActions
        );

        response.setTotalCallActions(
                phoneActions
        );

        response.setTotalActions(
                drivingDirectionActions
                        + websiteActions
                        + phoneActions
        );

        // =====================================================
        // STEP 8 : TOTAL VIEWS
        // =====================================================

        response.setDesktopTotalViews(
                desktopMapViews + desktopSearchViews
        );

        response.setMobileTotalViews(
                mobileMapViews + mobileSearchViews
        );

        response.setTotalViews(
                desktopMapViews
                        + mobileMapViews
                        + desktopSearchViews
                        + mobileSearchViews
        );

        return response;
    }


    /**
     * TABLE REPORT API
     */
    public InsightReportingTableDataResponse
    getInsightTableReportingData(
            InsightRequest request) {

        // =====================================================
        // STEP 1 : FETCH LOCATIONS
        // =====================================================

        List<DealerLocation> locations =
                getDealerLocations(request);

        // =====================================================
        // STEP 2 : SEARCH FILTER
        // =====================================================

        if (request.getSearch() != null
                && !request.getSearch()
                .trim()
                .isEmpty()) {

            String searchText =
                    request.getSearch()
                            .trim()
                            .toLowerCase();

            locations = locations.stream()
                    .filter(location ->
                            location.getDealerName() != null
                                    && location.getDealerName()
                                    .toLowerCase()
                                    .contains(searchText)
                    )
                    .collect(Collectors.toList());
        }

        // =====================================================
        // STEP 3 : CREATE DEALER IDS
        // =====================================================

        Set<String> dealerIds =
                getDealerIds(locations);

        // =====================================================
        // STEP 4 : EMPTY RESPONSE
        // =====================================================

        if (dealerIds.isEmpty()) {

            InsightReportingTableDataResponse
                    emptyResponse =
                    new InsightReportingTableDataResponse();

            emptyResponse.setGmbInsightLocationResponse(
                    new ArrayList<>()
            );

            emptyResponse.setTotalNoOfPages(0);

            emptyResponse.setTotalNoOfRecords(0L);

            return emptyResponse;
        }

        // =====================================================
        // STEP 5 : CREATE DEALER MAP
        // =====================================================

        Map<String, DealerLocation> dealerLocationMap =
                locations.stream()
                        .filter(location ->
                                location.getDealerId() != null)
                        .collect(Collectors.toMap(
                                DealerLocation::getDealerId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));

        // =====================================================
        // STEP 6 : FETCH INSIGHT DATA
        // =====================================================

        List<GMBLocationInsight> insightList =
                getFilteredInsight(request, dealerIds);

        // =====================================================
        // STEP 7 : GROUP BY DEALER ID
        // =====================================================

        Map<String, List<GMBLocationInsight>>
                dealerWiseData =
                insightList.stream()
                        .filter(insight ->
                                insight.getDealerId() != null)
                        .collect(Collectors.groupingBy(
                                GMBLocationInsight::getDealerId
                        ));

        // =====================================================
        // STEP 8 : PREPARE RESPONSE LIST
        // =====================================================

        List<GMBInsightLocationResponse> responseList =
                new ArrayList<>();

        for (Map.Entry<String,
                List<GMBLocationInsight>> entry
                : dealerWiseData.entrySet()) {

            String dealerId = entry.getKey();

            List<GMBLocationInsight> dealerInsights =
                    entry.getValue();

            DealerLocation dealerLocation =
                    dealerLocationMap.get(dealerId);

            GMBInsightLocationResponse response =
                    new GMBInsightLocationResponse();

            // =========================================
            // LOCATION NAME
            // =========================================

            if (dealerLocation != null) {

                response.setLocationName(
                        dealerLocation.getDealerName()
                );
            }

            // =========================================
            // SEARCH VIEWS
            // =========================================

            long searchViews =
                    sumField(
                            dealerInsights,
                            GMBLocationInsight::
                                    getViewsSearch
                    );

            response.setSearchViews(
                    searchViews
            );

            // =========================================
            // MAP VIEWS
            // =========================================

            long mapViews =
                    sumField(
                            dealerInsights,
                            GMBLocationInsight::
                                    getViewsMaps
                    );

            response.setMapViews(
                    mapViews
            );

            // =========================================
            // TOTAL VIEWS
            // =========================================

            response.setTotalViews(
                    searchViews + mapViews
            );

            // =========================================
            // DRIVING DIRECTION ACTIONS
            // =========================================

            long drivingDirectionActions =
                    sumField(
                            dealerInsights,
                            GMBLocationInsight::
                                    getActionsDrivingDirections
                    );

            response.setDrivingDirectionActions(
                    drivingDirectionActions
            );

            // =========================================
            // WEBSITE ACTIONS
            // =========================================

            long websiteActions =
                    sumField(
                            dealerInsights,
                            GMBLocationInsight::
                                    getActionsWebsite
                    );

            response.setWebsiteActions(
                    websiteActions
            );

            // =========================================
            // PHONE ACTIONS
            // =========================================

            long phoneActions =
                    sumField(
                            dealerInsights,
                            GMBLocationInsight::
                                    getActionsPhone
                    );


            // =========================================
            // TOTAL ACTIONS
            // =========================================

            response.setTotalActions(
                    drivingDirectionActions
                            + websiteActions
                            + phoneActions
            );

            responseList.add(response);
        }

        // =====================================================
        // STEP 9 : SORTING
        // =====================================================

        responseList.sort(
                Comparator.comparing(
                        GMBInsightLocationResponse::
                                getLocationName,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
        );

        // =====================================================
        // STEP 10 : PAGINATION
        // =====================================================

        int pageNo = request.getPageNo();


        int totalRecords =
                responseList.size();

        int start =
                pageNo * PAGE_SIZE;

        int end =
                Math.min(
                        start + PAGE_SIZE,
                        totalRecords
                );

        List<GMBInsightLocationResponse>
                paginatedList =
                new ArrayList<>();

        if (start < totalRecords) {

            paginatedList =
                    responseList.subList(start, end);
        }

        int totalPages =
                (int) Math.ceil(
                        (double) totalRecords
                                / PAGE_SIZE
                );

        // =====================================================
        // STEP 11 : FINAL RESPONSE
        // =====================================================

        InsightReportingTableDataResponse
                finalResponse =
                new InsightReportingTableDataResponse();

        finalResponse.setGmbInsightLocationResponse(
                paginatedList
        );

        finalResponse.setTotalNoOfPages(
                totalPages
        );

        finalResponse.setTotalNoOfRecords(
                (long) totalRecords
        );

        return finalResponse;
    }
    
    public InsightGraphResponse getInsightGraphData(
            InsightRequest request) {

        // =====================================================
        // STEP 1 : FETCH LOCATIONS
        // =====================================================

        List<DealerLocation> locations =
                getDealerLocations(request);

        // =====================================================
        // STEP 2 : CREATE DEALER MAP
        // =====================================================

        Map<String, DealerLocation> dealerLocationMap =
                locations.stream()
                        .filter(location ->
                                location.getDealerId() != null)
                        .collect(Collectors.toMap(
                                DealerLocation::getDealerId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));

        // =====================================================
        // STEP 3 : DEALER IDS
        // =====================================================

        Set<String> dealerIds =
                dealerLocationMap.keySet();

        InsightGraphResponse response =
                new InsightGraphResponse();

        if (dealerIds.isEmpty()) {
            return response;
        }

        // =====================================================
        // STEP 4 : FETCH INSIGHTS
        // =====================================================

        List<GMBLocationInsight> insightList =
                getFilteredInsight(request, dealerIds);

        // =====================================================
        // STEP 5 : CITYWISE VIEWS
        // =====================================================

        Map<String, Long> cityViewsMap =
                new HashMap<>();

        // =====================================================
        // STEP 6 : STATEWISE VIEWS
        // =====================================================

        Map<String, Long> stateViewsMap =
                new HashMap<>();

        // =====================================================
        // STEP 7 : CITYWISE ACTIONS
        // =====================================================

        Map<String, Long> cityActionsMap =
                new HashMap<>();

        // =====================================================
        // STEP 8 : STATEWISE ACTIONS
        // =====================================================

        Map<String, Long> stateActionsMap =
                new HashMap<>();

        // =====================================================
        // STEP 9 : LOOP INSIGHTS
        // =====================================================

        for (GMBLocationInsight insight : insightList) {

            DealerLocation dealerLocation =
                    dealerLocationMap.get(
                            insight.getDealerId()
                    );

            if (dealerLocation == null) {
                continue;
            }

            String city =
                    dealerLocation.getCity();

            String state =
                    dealerLocation.getState();

            if (city == null || city.trim().isEmpty()) {
                city = "Unknown";
            }

            if (state == null || state.trim().isEmpty()) {
                state = "Unknown";
            }

            // =========================================
            // TOTAL VIEWS
            // =========================================

            long totalViews =
                    Optional.ofNullable(
                            insight.getViewsSearch()
                    ).orElse(0L)
                            +
                            Optional.ofNullable(
                                    insight.getViewsMaps()
                            ).orElse(0L);

            // =========================================
            // TOTAL ACTIONS
            // =========================================

            long totalActions =
                    Optional.ofNullable(
                            insight.getActionsDrivingDirections()
                    ).orElse(0L)
                            +
                            Optional.ofNullable(
                                    insight.getActionsWebsite()
                            ).orElse(0L)
                            +
                            Optional.ofNullable(
                                    insight.getActionsPhone()
                            ).orElse(0L);

            // =========================================
            // CITYWISE VIEWS
            // =========================================

            cityViewsMap.merge(
                    city,
                    totalViews,
                    Long::sum
            );

            // =========================================
            // STATEWISE VIEWS
            // =========================================

            stateViewsMap.merge(
                    state,
                    totalViews,
                    Long::sum
            );

            // =========================================
            // CITYWISE ACTIONS
            // =========================================

            cityActionsMap.merge(
                    city,
                    totalActions,
                    Long::sum
            );

            // =========================================
            // STATEWISE ACTIONS
            // =========================================

            stateActionsMap.merge(
                    state,
                    totalActions,
                    Long::sum
            );
        }

        // =====================================================
        // STEP 10 : BUILD RESPONSE
        // =====================================================

        response.setCitywiseViews(
                buildGraphData(cityViewsMap)
        );

        response.setStatewiseViews(
                buildGraphData(stateViewsMap)
        );

        response.setCitywiseActions(
                buildGraphData(cityActionsMap)
        );

        response.setStatewiseActions(
                buildGraphData(stateActionsMap)
        );

        return response;
    }
    
    private GraphData buildGraphData(
            Map<String, Long> dataMap) {

        GraphData graphData =
                new GraphData();

        if (dataMap == null || dataMap.isEmpty()) {

            graphData.setChartData(
                    new ArrayList<>()
            );

            graphData.setPieData(
                    new ArrayList<>()
            );

            return graphData;
        }

        // =====================================================
        // SORT DESC
        // =====================================================

        List<Map.Entry<String, Long>> sortedList =
                dataMap.entrySet()
                        .stream()
                        .sorted(
                                Map.Entry
                                        .<String, Long>
                                                comparingByValue()
                                        .reversed()
                        )
                        .collect(Collectors.toList());

        // =====================================================
        // TOP 10 + OTHERS
        // =====================================================

        List<ChartData> chartDataList =
                new ArrayList<>();

        long others = 0L;

        int count = 0;

        long total =
                dataMap.values()
                        .stream()
                        .mapToLong(Long::longValue)
                        .sum();

        for (Map.Entry<String, Long> entry
                : sortedList) {

            if (count < 10) {

                chartDataList.add(
                        new ChartData(
                                entry.getKey(),
                                entry.getValue()
                        )
                );

            } else {

                others += entry.getValue();
            }

            count++;
        }

        if (others > 0) {

            chartDataList.add(
                    new ChartData(
                            "Others",
                            others
                    )
            );
        }

        // =====================================================
        // PIE DATA
        // =====================================================

        List<PieData> pieDataList =
                chartDataList.stream()
                        .map(data -> {

                            double percentage =
                                    total == 0
                                            ? 0
                                            : ((double) data.getValue()
                                            / total) * 100;

                            percentage =
                                    Math.round(
                                            percentage * 100.0
                                    ) / 100.0;

                            return new PieData(
                                    data.getName(),
                                    percentage
                            );
                        })
                        .collect(Collectors.toList());

        graphData.setChartData(
                chartDataList
        );

        graphData.setPieData(
                pieDataList
        );

        return graphData;
    }
    public List<GMBReview> getFilteredReview(ReviewRequest req) {
    	
        Date startDate = req.getStartDate();
        Date endDate = req.getEndDate();

        // If dates are not provided, set default last 1 month range
        if (startDate == null || endDate == null) {

            Calendar calendar = Calendar.getInstance();

            // End date = current date/time
            endDate = calendar.getTime();

            // Start date = 1 month before
            calendar.add(Calendar.MONTH, -1);
            startDate = calendar.getTime();
        }

        List<GMBReview> reviews =
                gmbReviewRepository.findReviewsByClientAndDateRange(
                        req.getClientId(),
                        req.getStartDate(),
                        req.getEndDate()
                );

        System.out.println("SIZE = " + reviews.size());

        //return reviews;
        
        return gmbReviewRepository.findReviewsByClientAndDateRange(req.getClientId(), startDate, endDate);
    }
    
    public ReviewReportingDataPageResponse getReviewReportingData(ReviewRequest request) {

        // ================= VALIDATION =================

        if (request == null) {
            throw new InvalidRequestException("Request body cannot be null");
        }

        List<GMBReview> allGMBReviews;

        try {
            allGMBReviews = getFilteredReview(request);
        } catch (Exception ex) {
            throw new RuntimeException("Error while fetching review data", ex);
        }

        if (allGMBReviews == null) {
            throw new ReviewNotFoundException("No review data found");
        }

        // ================= REVIEW OVERVIEW =================

        long totalReviews = allGMBReviews.size();

        long reviewsRepliedAfterMonth = allGMBReviews.stream()
                .filter(review ->
                        review != null &&
                        review.getReplyTime() != null &&
                        review.getCreatedTime() != null &&
                        review.getReplyTime().after(
                                new Date(
                                        review.getCreatedTime().getTime()
                                                + (30L * 24 * 60 * 60 * 1000)
                                )
                        )
                )
                .count();

        double avgReview = allGMBReviews.stream()
                .filter(Objects::nonNull)
                .mapToLong(review -> review.getStarRating())
                .average()
                .orElse(0.0);

        avgReview = Math.round(avgReview * 100.0) / 100.0;

        long unrepliedCount = allGMBReviews.stream()
                .filter(review ->
                        review != null &&
                        (review.getReplyComment() == null ||
                                review.getReplyComment().trim().isEmpty())
                )
                .count();

        double unrepliedReview = totalReviews == 0
                ? 0
                : ((double) unrepliedCount * 100) / totalReviews;

        unrepliedReview = Math.round(unrepliedReview * 100.0) / 100.0;

        ReviewOverViewData reviewOverViewData = ReviewOverViewData.builder()
                .ReviewsRepliedAfterMonth(reviewsRepliedAfterMonth)
                .avgReview(avgReview)
                .unrepliedReview(unrepliedReview)
                .build();

        // ================= SENTIMENT REVIEW =================

        long positive = allGMBReviews.stream()
                .filter(review ->
                        review != null &&
                        (review.getStarRating() == 4 ||
                                review.getStarRating() == 5)
                )
                .count();

        long negative = allGMBReviews.stream()
                .filter(review ->
                        review != null &&
                        (review.getStarRating() == 1 ||
                                review.getStarRating() == 2)
                )
                .count();

        long neutral = allGMBReviews.stream()
                .filter(review ->
                        review != null &&
                        review.getStarRating() == 3
                )
                .count();

        List<SentimentReview> sentimentReviews = List.of(

                SentimentReview.builder()
                        .name("Positive")
                        .value(positive)
                        .build(),

                SentimentReview.builder()
                        .name("Negative")
                        .value(negative)
                        .build(),

                SentimentReview.builder()
                        .name("Neutral")
                        .value(neutral)
                        .build()
        );

        // ================= COMMENT SPLIT REVIEW =================

        long blankComments = allGMBReviews.stream()
                .filter(review ->
                        review != null &&
                        (review.getComment() == null ||
                                review.getComment().trim().isEmpty())
                )
                .count();

        long withComments = allGMBReviews.stream()
                .filter(review ->
                        review != null &&
                        review.getComment() != null &&
                        !review.getComment().trim().isEmpty()
                )
                .count();

        List<CommentSplitReview> commentSplitReviews = List.of(

                CommentSplitReview.builder()
                        .name("Blank")
                        .value(blankComments)
                        .build(),

                CommentSplitReview.builder()
                        .name("With Comments")
                        .value(withComments)
                        .build()
        );

        // ================= REVIEW GRAPH DATA =================

        Map<LocalDate, List<GMBReview>> groupedReviews;

        try {

            groupedReviews = allGMBReviews.stream()
                    .filter(review ->
                            review != null &&
                            review.getCreatedTime() != null
                    )
                    .collect(Collectors.groupingBy(
                            review -> review.getCreatedTime()
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                    ));

        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid review date data found");
        }

        List<ReviewGraphData> reviewGraphDataList = groupedReviews.entrySet()
                .stream()
                .map(entry -> {

                    LocalDate date = entry.getKey();
                    List<GMBReview> reviews = entry.getValue();

                    long dailyTotalReviews = reviews.size();

                    double avgRating = reviews.stream()
                            .filter(Objects::nonNull)
                            .mapToLong(GMBReview::getStarRating)
                            .average()
                            .orElse(0.0);

                    avgRating = Math.round(avgRating * 100.0) / 100.0;

                    return ReviewGraphData.builder()
                            .date(date)
                            .totalReviews(dailyTotalReviews)
                            .avgRating(avgRating)
                            .build();
                })
                .sorted(Comparator.comparing(ReviewGraphData::getDate))
                .collect(Collectors.toList());

        // ================= RATING COUNT DATA =================

        List<RatingCountData> ratingCountData = IntStream.rangeClosed(1, 5)
                .mapToObj(star -> {

                    long count = allGMBReviews.stream()
                            .filter(review ->
                                    review != null &&
                                    review.getStarRating() == star
                            )
                            .count();

                    double percentage = totalReviews == 0
                            ? 0
                            : ((double) count * 100) / totalReviews;

                    percentage = Math.round(percentage * 100.0) / 100.0;

                    return RatingCountData.builder()
                            .stars(star)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());

        // ================= REVIEW TABLE DATA =================

        List<DealerLocation> dealerLocations =
                dealerLocationRepository.findByClientId(request.getClientId());

        Map<String, DealerLocation> dealerLocationMap =
                dealerLocations.stream()
                        .filter(loc -> loc.getDealerId() != null)
                        .collect(Collectors.toMap(
                                DealerLocation::getDealerId,
                                Function.identity(),
                                (existing, replacement) -> existing));

        List<GMBReview> sortedReviews = allGMBReviews.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(GMBReview::getCreatedTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        int pageNo = request.getPageNo();
        int start = pageNo * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, sortedReviews.size());

        List<GMBReview> paginatedReviews =
                start >= sortedReviews.size() ? new ArrayList<>() : sortedReviews.subList(start, end);

        int totalReviewPages = (int) Math.ceil((double) totalReviews / PAGE_SIZE);

        List<ReviewTableRowData> reviewTableRowList = paginatedReviews.stream()
                .map(review -> {
                    DealerLocation loc = dealerLocationMap.get(review.getDealerId());
                    return ReviewTableRowData.builder()
                            .dealerId(review.getDealerId())
                            .dealerName(loc != null ? loc.getDealerName() : null)
                            .reviewer(review.getReviewer())
                            .city(loc != null ? loc.getCity() : null)
                            .comment(review.getComment())
                            .reply(review.getReplyComment())
                            .rating(review.getStarRating())
                            .date(review.getCreatedTime())
                            .replyDate(review.getReplyTime())
                            .build();
                })
                .collect(Collectors.toList());

        ReviewTableData reviewTableData = ReviewTableData.builder()
                .data(reviewTableRowList)
                .totalNoPages(totalReviewPages)
                .build();

        // ================= FINAL RESPONSE =================

        return ReviewReportingDataPageResponse.builder()
                .reviewOverViewData(reviewOverViewData)
                .sentimentReview(sentimentReviews)
                .commentSplitReview(commentSplitReviews)
                .reviewGraphData(reviewGraphDataList)
                .ratingCountData(ratingCountData)
                .reviewTableData(reviewTableData)
                .build();
    }

    /**
     * GMB LOCATION REPORT API
     */
    public GMBLocationReportPageResponse getGMBLocationReportData(
            GMBLocationReportRequest request) {

        // =====================================================
        // STEP 1 : FETCH DEALER LOCATIONS MAPPED TO USER + CLIENT
        // =====================================================

        List<DealerLocation> dealerLocations =
                dealerLocationService.getFilteredDealerLocationByUserId(
                        request.getClientId(),
                        request.getUserId()
                );

        List<String> dealerIds =
                dealerLocations.stream()
                        .map(DealerLocation::getDealerId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        if (dealerIds.isEmpty()) {
            return emptyGMBLocationReportPageResponse();
        }

        Map<String, DealerLocation> dealerLocationMap =
                dealerLocations.stream()
                        .filter(loc -> loc.getDealerId() != null)
                        .collect(Collectors.toMap(
                                DealerLocation::getDealerId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));

        // =====================================================
        // STEP 2 : FETCH GMB LOCATIONS FOR THOSE DEALER IDS
        // =====================================================

        List<GMBLocation> gmbLocations =
                gmbLocationRepository.findByClientIdAndDealerIdIn(
                        request.getClientId(),
                        dealerIds
                );

        // =====================================================
        // STEP 3 : SEARCH FILTER
        // =====================================================

        if (request.getSearch() != null
                && !request.getSearch()
                .trim()
                .isEmpty()) {

            String searchText =
                    request.getSearch()
                            .trim()
                            .toLowerCase();

            gmbLocations = gmbLocations.stream()
                    .filter(location ->
                            location.getName() != null
                                    && location.getName()
                                    .toLowerCase()
                                    .contains(searchText)
                    )
                    .collect(Collectors.toList());
        }

        // =====================================================
        // STEP 4 : EMPTY RESPONSE
        // =====================================================

        if (gmbLocations.isEmpty()) {
            return emptyGMBLocationReportPageResponse();
        }

        // =====================================================
        // STEP 5 : CAMPAIGN SETUP MAP
        // =====================================================

        List<ClientLocationSetup> clientLocationSetups =
                clientLocationSetupRepository.findByClientId(
                        request.getClientId()
                );

        Map<String, ClientLocationSetup> clientLocationSetupMap =
                clientLocationSetups.stream()
                        .filter(setup -> setup.getDealerId() != null)
                        .collect(Collectors.toMap(
                                ClientLocationSetup::getDealerId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));

        // =====================================================
        // STEP 6 : BUILD RESPONSE LIST
        // =====================================================

        List<GMBLocationReportResponse> responseList =
                gmbLocations.stream()
                        .map(location -> mapToGMBLocationReportResponse(
                                location,
                                clientLocationSetupMap.get(location.getDealerId()),
                                dealerLocationMap.get(location.getDealerId())
                        ))
                        .collect(Collectors.toList());

        // =====================================================
        // STEP 7 : SORTING
        // =====================================================

        responseList.sort(
                Comparator.comparing(
                        GMBLocationReportResponse::getLocationName,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
        );

        // =====================================================
        // STEP 8 : PAGINATION
        // =====================================================

        int pageNo = request.getPageNo();

        int totalRecords = responseList.size();

        int start = pageNo * PAGE_SIZE;

        int end = Math.min(start + PAGE_SIZE, totalRecords);

        List<GMBLocationReportResponse> paginatedList =
                start < totalRecords
                        ? responseList.subList(start, end)
                        : new ArrayList<>();

        int totalPages =
                (int) Math.ceil((double) totalRecords / PAGE_SIZE);

        // =====================================================
        // STEP 9 : FINAL RESPONSE
        // =====================================================

        GMBLocationReportPageResponse finalResponse =
                new GMBLocationReportPageResponse();

        finalResponse.setGmbLocationReportResponse(paginatedList);

        finalResponse.setTotalNoOfPages(totalPages);

        finalResponse.setTotalNoOfRecords((long) totalRecords);

        return finalResponse;
    }

    private GMBLocationReportPageResponse emptyGMBLocationReportPageResponse() {

        GMBLocationReportPageResponse emptyResponse =
                new GMBLocationReportPageResponse();

        emptyResponse.setGmbLocationReportResponse(new ArrayList<>());

        emptyResponse.setTotalNoOfPages(0);

        emptyResponse.setTotalNoOfRecords(0L);

        return emptyResponse;
    }

    private GMBLocationReportResponse mapToGMBLocationReportResponse(
            GMBLocation location,
            ClientLocationSetup clientLocationSetup,
            DealerLocation dealerLocation) {

        return GMBLocationReportResponse.builder()
                .locationName(location.getName())
                .status(location.getStatus())
                .campaignSetup(clientLocationSetup != null)
                .healthScore(gbpCompletenessScoreCalculator.calculateScore(location))
                .emailId(dealerLocation != null ? dealerLocation.getEmail() : null)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .phoneNumber(location.getPhoneNumber())
                .area(location.getArea())
                .city(location.getCity())
                .state(location.getState())
                .countryCode(location.getCountryCode())
                .pincode(location.getPincode())
                .address1(location.getAddress1())
                .websiteUrl(location.getWebsiteUrl())
                .campaignPhoneNumber(
                        clientLocationSetup != null
                                ? clientLocationSetup.getClientCampaignPhoneNumber()
                                : null
                )
                .description(location.getDescription())
                .gmbOperationHours(
                        mapToOperationHoursResponse(location.getGmbOperationHours())
                )
                .landingPageUrl(
                        clientLocationSetup != null
                                ? clientLocationSetup.getLandingPageUrl()
                                : null
                )
                .youtubeUrl(location.getYoutubeUrl())
                .appointmentLink(location.getAppointmentLink())
                .whatsappUrl(location.getWhatsappUrl())
                .build();
    }

    private GMBOperationHoursResponse mapToOperationHoursResponse(
            GMBOperationHours hours) {

        if (hours == null) {
            return null;
        }

        return GMBOperationHoursResponse.builder()
                .mondayOpenTime(hours.getMondayOpenTime())
                .mondayCloseTime(hours.getMondayCloseTime())
                .tuesdayOpenTime(hours.getTuesdayOpenTime())
                .tuesdayCloseTime(hours.getTuesdayCloseTime())
                .wednesdayOpenTime(hours.getWednesdayOpenTime())
                .wednesdayCloseTime(hours.getWednesdayCloseTime())
                .thursdayOpenTime(hours.getThursdayOpenTime())
                .thursdayCloseTime(hours.getThursdayCloseTime())
                .fridayOpenTime(hours.getFridayOpenTime())
                .fridayCloseTime(hours.getFridayCloseTime())
                .saturdayOpenTime(hours.getSaturdayOpenTime())
                .saturdayCloseTime(hours.getSaturdayCloseTime())
                .sundayOpenTime(hours.getSundayOpenTime())
                .sundayCloseTime(hours.getSundayCloseTime())
                .build();
    }
}