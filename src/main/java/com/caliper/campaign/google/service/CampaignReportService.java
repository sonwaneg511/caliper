package com.caliper.campaign.google.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.campaign.google.dto.request.CampaignReportActionRequest;
import com.caliper.campaign.google.dto.response.CampaignDailyReport;
import com.caliper.campaign.google.dto.response.CampaignReport;
import com.caliper.campaign.google.dto.response.CampaignReportDaily;
import com.caliper.campaign.google.dto.response.CampaignReportResponse;
import com.caliper.campaign.google.dto.response.CampaignWiseData;
import com.caliper.campaign.google.dto.response.ConsolidateCampaignReport;
import com.caliper.campaign.google.dto.response.Report;
import com.caliper.campaign.google.entity.ArtAdwordsData;
import com.caliper.campaign.google.entity.ClientAccountSetup;
import com.caliper.campaign.google.entity.GoogleCampaign;
import com.caliper.campaign.google.repository.ClientAccountSetupRepository;
import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.service.DealerLocationService;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.google.cloud.bigquery.JobException;

@Service
public class CampaignReportService {

	private static final int PAGE_SIZE = 10;
    @Autowired
    private CampaignService campaignService;

    @Autowired
    private ClientAccountSetupRepository clientAccountSetupRepository;

    @Autowired
    private DealerLocationService dealerLocationService;

    /**
     * Builds LocationFilterRequest from incoming API request
     */
    public LocationFilterRequest locationFilterRequestCreate(CampaignReportActionRequest req) {
        return LocationFilterRequest.builder()
                .clientId(req.getClientId())
                .userId(req.getUserId())
                .build();
    }

    /**
     * Main entry point to generate campaign report
     * Steps:
     * 1. Fetch client configuration
     * 2. Fetch dealer locations
     * 3. Fetch campaigns
     * 4. Fetch Adwords stats
     * 5. Prepare response (campaign-wise, consolidated, daily)
     */
    public CampaignReportResponse process(CampaignReportActionRequest parsedRequest)
            throws SQLException, ParseException, FileNotFoundException,
            JobException, IOException, InterruptedException {

        // Fetch client setup (mandatory)
        ClientAccountSetup clientDataByClientName = clientAccountSetupRepository
                .findByClientId(parsedRequest.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + parsedRequest.getClientId()));

        // Get dealer locations for filtering campaigns
        LocationFilterRequest locationRequest = locationFilterRequestCreate(parsedRequest);
        List<DealerLocation> locations =
                dealerLocationService.getFilteredDealerLocation(locationRequest);

        List<String> dealerIds = locations.stream()
                .map(DealerLocation::getDealerId)
                .collect(Collectors.toList());

        // Fetch campaigns for client & dealers
        List<GoogleCampaign> allGoogleCampaign =
                campaignService.findAllGoogleCampaignByClientIdAndDealerIds(
                        clientDataByClientName.getClientId(), dealerIds);

        if (allGoogleCampaign == null || allGoogleCampaign.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No campaigns found for client: " + parsedRequest.getClientId());
        }

        // Fetch raw Adwords stats from BigQuery
        List<ArtAdwordsData> CampaignBasicStatsList =
                campaignService.getArtAdwordsData(
                        Long.valueOf(clientDataByClientName.getGoogleAccountId()),
                        parsedRequest.getStartDate(),
                        parsedRequest.getEndDate());

        try {
            // Generate different report views
            CampaignReport campaignReports =
                    campaignWiseData(CampaignBasicStatsList, clientDataByClientName, allGoogleCampaign, parsedRequest.getPageNo(),PAGE_SIZE);

            List<ConsolidateCampaignReport> consolidateCampaignReports =
                    consolidateData(CampaignBasicStatsList, clientDataByClientName, allGoogleCampaign);

            CampaignDailyReport campaignDailyTotalReports =
                    dateWiseTotalData(CampaignBasicStatsList, clientDataByClientName,
                            allGoogleCampaign, parsedRequest, PAGE_SIZE);

            List<Report> reportList = new ArrayList<>();
            
            Report report = Report.builder().campaignReport(campaignReports).campaignDailyReports(campaignDailyTotalReports).build();
            
            return CampaignReportResponse.builder()
                    .status("SUCCESS")
                    .message("Report Generated")
                    .clientId(parsedRequest.getClientId())
                    .startDate(parsedRequest.getStartDate())
                    .endDate(parsedRequest.getEndDate())
                    .consolidateCampaignReport(consolidateCampaignReports)
                    .report(report)
                    .build();

        } catch (Exception e) {
            // Wrap all failures into a business exception
            throw new ResourceNotFoundException("Failed to generate campaign report");
        }
    }

    /**
     * Consolidated report across accounts (grouped by uniqueId)
     * Aggregates total clicks, impressions, cost, etc.
     */
    private List<ConsolidateCampaignReport> consolidateData(
            List<ArtAdwordsData> CampaignBasicStatsList,
            ClientAccountSetup clientDataByClientName,
            List<GoogleCampaign> allGoogleCampaign) {

        // Group stats by account (uniqueId)
        Map<String, List<ArtAdwordsData>> groupedByAccount =
                CampaignBasicStatsList.stream()
                        .collect(Collectors.groupingBy(ArtAdwordsData::getUniqueId));

        List<ConsolidateCampaignReport> result = new ArrayList<>();

        for (Entry<String, List<ArtAdwordsData>> entry : groupedByAccount.entrySet()) {

            String accountId = entry.getKey();
            List<ArtAdwordsData> stats = entry.getValue();

            // Aggregate metrics
            double totalClicks = stats.stream().mapToDouble(ArtAdwordsData::getClicks).sum();
            double totalImpressions = stats.stream().mapToDouble(ArtAdwordsData::getImpressions).sum();
            double cost = stats.stream().mapToDouble(ArtAdwordsData::getCost).sum();
            double videoViews = stats.stream().mapToDouble(ArtAdwordsData::getVideoViews).sum();

            // Derived metrics
            double ctr = totalImpressions == 0 ? 0 : (totalClicks / totalImpressions) * 100;
            double cpm = totalImpressions == 0 ? 0 : (cost * 1000) / totalImpressions;
            double cpcBid = clientDataByClientName != null ? clientDataByClientName.getCpcBid() : 0;

            // Planned metrics
            double plannedClick = (cpcBid == 0) ? 0 :
                    allGoogleCampaign.stream()
                            .filter(c -> accountId.equals(c.getGoogleAccountID()))
                            .mapToDouble(c -> c.getTotalBudget().doubleValue() / cpcBid)
                            .sum();

            double plannedCost = allGoogleCampaign.stream()
                    .filter(c -> accountId.equals(c.getGoogleAccountID()))
                    .mapToDouble(c -> c.getTotalBudget().doubleValue())
                    .sum();

            double vtr = totalImpressions == 0 ? 0 : (videoViews * 100) / totalImpressions;

            result.add(ConsolidateCampaignReport.builder()
                    .totalCost(cost)
                    .totalPlannedCost(plannedCost)
                    .totalDeliveredClicks(totalClicks)
                    .totalVideoViews(videoViews)
                    .ctr(ctr)
                    .cpm(cpm)
                    .vtr(vtr)
                    .build());
        }

        return result;
    }

    /**
     * Campaign-wise report
     * Each campaign is filtered and aggregated separately
     */
    private CampaignReport campaignWiseData(
            List<ArtAdwordsData> CampaignBasicStatsList,
            ClientAccountSetup clientDataByClientName,
            List<GoogleCampaign> allGoogleCampaign,
            int pageNumber,
            int pageSize) {

        List<CampaignWiseData> campaignWiseData = new ArrayList<>();

        for (GoogleCampaign campaign : allGoogleCampaign) {

            if (campaign.getCampaignResourceName().equalsIgnoreCase("-1")) continue;

            String[] split = campaign.getCampaignResourceName().split("/");
            if (split.length < 4) continue;
            String campaignId = split[3];

            Map<String, List<ArtAdwordsData>> grouped =
                    CampaignBasicStatsList.stream()
                            .filter(s -> campaignId.equalsIgnoreCase(s.getCampaignId()))
                            .collect(Collectors.groupingBy(ArtAdwordsData::getCampaignName));

            for (List<ArtAdwordsData> stats : grouped.values()) {

                double cost = stats.stream().mapToDouble(ArtAdwordsData::getCost).sum();
                double clicks = stats.stream().mapToDouble(ArtAdwordsData::getClicks).sum();
                double impressions = stats.stream().mapToDouble(ArtAdwordsData::getImpressions).sum();
                double conversions = stats.stream().mapToDouble(ArtAdwordsData::getConversions).sum();
                double videoViews = stats.stream().mapToDouble(ArtAdwordsData::getVideoViews).sum();

                double ctr = impressions == 0 ? 0 : (clicks / impressions) * 100;
                double cpm = impressions == 0 ? 0 : (cost * 1000) / impressions;
                double avgCpc = clicks == 0 ? 0 : cost / clicks;
                double costPerConversion = conversions == 0 ? 0 : cost / conversions;
                double vtr = impressions == 0 ? 0 : (videoViews * 100) / impressions;
                double costPerView = videoViews == 0 ? 0 : cost / videoViews;

                campaignWiseData.add(CampaignWiseData.builder()
                        .campaignName(campaign.getCampaignName())
                        .creationDate(campaign.getLastModidfiedDate())
                        .cost(cost)
                        .plannedCost(campaign.getTotalBudget())
                        .deliveredImpressions(impressions)
                        .deliveredClicks(clicks)
                        .videoViews(videoViews)
                        .conversions(conversions)
                        .ctr(ctr)
                        .vtr(vtr)
                        .costPerConversion(costPerConversion)
                        .costPerMile(cpm)
                        .costPerView(costPerView)
                        .partnerName(stats.isEmpty() ? "" : stats.get(0).getPartnerName())
                        .build());
            }
        }

        // ✅ Pagination Logic
        int totalRecords = campaignWiseData.size();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

        int fromIndex = pageNumber * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalRecords);

        List<CampaignWiseData> paginatedList = new ArrayList<>();

        if (fromIndex < totalRecords) {
            paginatedList = campaignWiseData.subList(fromIndex, toIndex);
        }

        // ✅ Build final response
        return CampaignReport.builder()
                .campaign_wise_data(paginatedList)
                .totalNoOfPages(totalPages)
                .totalNoOfRecords(totalRecords)
                .build();
    }

    /**
     * Date-wise aggregation across all campaigns
     * Steps:
     * 1. Filter stats by date range
     * 2. Group by date
     * 3. Merge across campaigns per day
     */
    private CampaignDailyReport dateWiseTotalData(
            List<ArtAdwordsData> CampaignBasicStatsList,
            ClientAccountSetup clientDataByClientName,
            List<GoogleCampaign> allGoogleCampaign,
            CampaignReportActionRequest parsedRequest,
            int pageSize) {

        List<CampaignReportDaily> dailyReports = new ArrayList<>();

        Date startDate = parsedRequest.getStartDate();
        Date endDate = parsedRequest.getEndDate();

        for (GoogleCampaign campaign : allGoogleCampaign) {

            if (campaign.getCampaignResourceName().equalsIgnoreCase("-1")) continue;

            String[] resourceNameParts = campaign.getCampaignResourceName().split("/");
            if (resourceNameParts.length < 4) continue;
            String campaignId = resourceNameParts[3];

            List<ArtAdwordsData> filteredStats =
                    CampaignBasicStatsList.stream()
                            .filter(stat ->
                                    stat.getDate().compareTo(startDate) >= 0 &&
                                    stat.getDate().compareTo(endDate) <= 0 &&
                                    campaignId.equals(stat.getCampaignId()))
                            .collect(Collectors.toList());

            Map<Date, List<ArtAdwordsData>> groupedByDate =
                    filteredStats.stream()
                            .collect(Collectors.groupingBy(ArtAdwordsData::getDate));

            for (Entry<Date, List<ArtAdwordsData>> entry : groupedByDate.entrySet()) {

                Date date = entry.getKey();
                List<ArtAdwordsData> stats = entry.getValue();

                double clicks = stats.stream().mapToDouble(ArtAdwordsData::getClicks).sum();
                double impressions = stats.stream().mapToDouble(ArtAdwordsData::getImpressions).sum();
                double cost = stats.stream().mapToDouble(ArtAdwordsData::getCost).sum();
                double conversions = stats.stream().mapToDouble(ArtAdwordsData::getConversions).sum();
                double videoViews = stats.stream().mapToDouble(ArtAdwordsData::getVideoViews).sum();

                double ctr = impressions == 0 ? 0 : (clicks / impressions) * 100;
                double cpm = impressions == 0 ? 0 : (cost * 1000) / impressions;
                double vtr = impressions == 0 ? 0 : (videoViews * 100) / impressions;

                dailyReports.add(CampaignReportDaily.builder()
                        .date(date)
                        .cost(cost)
                        .deliveredImpressions(impressions)
                        .deliveredClicks(clicks)
                        .videoViews(videoViews)
                        .conversions(conversions)
                        .ctr(ctr)
                        .vtr(vtr)
                        .cpm(cpm)
                        .build());
            }
        }

        // ✅ Merge campaigns into single date
        Map<Date, CampaignReportDaily> merged =
                dailyReports.stream().collect(Collectors.toMap(
                        CampaignReportDaily::getDate,
                        r -> r,
                        (r1, r2) -> {
                            double cost = r1.getCost() + r2.getCost();
                            double clicks = r1.getDeliveredClicks() + r2.getDeliveredClicks();
                            double impressions = r1.getDeliveredImpressions() + r2.getDeliveredImpressions();
                            double conversions = r1.getConversions() + r2.getConversions();
                            double videoViews = r1.getVideoViews() + r2.getVideoViews();

                            return CampaignReportDaily.builder()
                                    .date(r1.getDate())
                                    .cost(cost)
                                    .deliveredClicks(clicks)
                                    .deliveredImpressions(impressions)
                                    .conversions(conversions)
                                    .videoViews(videoViews)
                                    .ctr(impressions == 0 ? 0 : (clicks / impressions) * 100)
                                    .cpm(impressions == 0 ? 0 : (cost * 1000) / impressions)
                                    .vtr(impressions == 0 ? 0 : (videoViews * 100) / impressions)
                                    .build();
                        }));

        // ✅ Sort by date
        List<CampaignReportDaily> sortedList = merged.values().stream()
                .sorted(Comparator.comparing(CampaignReportDaily::getDate))
                .collect(Collectors.toList());

        // ✅ Pagination
        int totalRecords = sortedList.size();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

        int fromIndex = parsedRequest.getPageNo() * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalRecords);

        List<CampaignReportDaily> paginatedList = new ArrayList<>();

        if (fromIndex < totalRecords) {
            paginatedList = sortedList.subList(fromIndex, toIndex);
        }

        // ✅ Final response
        return CampaignDailyReport.builder()
                .campaignReportDaily(paginatedList)
                .totalNoOfPages(totalPages)
                .totalNoOfRecords(totalRecords)
                .build();
    }
}
