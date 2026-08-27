package com.caliper.reporting.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.caliper.campaign.google.dto.request.CampaignReportActionRequest;
import com.caliper.campaign.google.dto.response.CampaignReportResponse;
import com.caliper.campaign.google.service.CampaignReportService;
import com.caliper.post.dto.Request.PostRequest;
import com.caliper.reporting.dto.request.GMBLocationInisghtRequest;
import com.caliper.reporting.dto.request.GMBLocationReportRequest;
import com.caliper.reporting.dto.request.InsightRequest;
import com.caliper.reporting.dto.response.GMBInsightLocationSumDataResponse;
import com.caliper.reporting.dto.response.GMBLocationInsightResponse;
import com.caliper.reporting.dto.response.GMBLocationReportPageResponse;
import com.caliper.reporting.dto.response.InsightGraphResponse;
import com.caliper.reporting.dto.response.InsightReportingTableDataResponse;
import com.caliper.reporting.dto.response.PostReportingDataPageResponse;
import com.caliper.reporting.dto.response.ReviewReportingDataPageResponse;
import com.caliper.reporting.service.GMBInsightService;
import com.caliper.reporting.service.ReportingService;
import com.caliper.review.dto.request.ReviewRequest;
import com.caliper.review.entity.GMBReview;
import com.google.cloud.bigquery.JobException;
@Controller
@RequestMapping("/reporting")
public class ReportingController {

	@Autowired
	private GMBInsightService gmbInsightService;
	
	@Autowired
	private ReportingService reportingService;
	
	@Autowired
	private CampaignReportService campaignReportService;
	
	@PostMapping("/insight")
	public ResponseEntity<GMBLocationInsightResponse> getInsightLocationData(@RequestBody GMBLocationInisghtRequest req) {
		GMBLocationInsightResponse locationInsightData = gmbInsightService.getLocationInsightData(req);
		return ResponseEntity.ok(locationInsightData);
	}
	
	@PostMapping("/post")
	public ResponseEntity<PostReportingDataPageResponse> postGraphRequest(@RequestBody PostRequest postGraphRequest) {
		PostReportingDataPageResponse postGraphResponse = reportingService.getPostReportingData(postGraphRequest);
		return ResponseEntity.ok(postGraphResponse);
	}
	
	@PostMapping("/campaign-report")
	public ResponseEntity<CampaignReportResponse> getCampaignReport(@RequestBody CampaignReportActionRequest parsedRequest) throws FileNotFoundException, JobException, SQLException, ParseException, IOException, InterruptedException {
		CampaignReportResponse process = campaignReportService.process(parsedRequest);
		return ResponseEntity.ok(process);
	}
	
	@PostMapping("/insight-table-data")
	public ResponseEntity<InsightReportingTableDataResponse> getInsightLocationTableData(@RequestBody InsightRequest request) {
		InsightReportingTableDataResponse locationInsightData = reportingService.getInsightTableReportingData(request);
		return ResponseEntity.ok(locationInsightData);
	}
	
	@PostMapping("/insight-sum-data")
	public ResponseEntity<GMBInsightLocationSumDataResponse> getInsightLocationSumData(@RequestBody InsightRequest request) {
		GMBInsightLocationSumDataResponse locationInsightData = reportingService.getInsightSumData(request);
		return ResponseEntity.ok(locationInsightData);
	}
	
	@PostMapping("/insight-graph-data")
	public ResponseEntity<InsightGraphResponse> getInsightLocationGraphData(@RequestBody InsightRequest request) {
		InsightGraphResponse locationInsightData = reportingService.getInsightGraphData(request);
		return ResponseEntity.ok(locationInsightData);
	}
	@PostMapping("/filtered")
	    public ResponseEntity<List<GMBReview>> getFilteredReviews(@RequestBody ReviewRequest request) {
	        List<GMBReview> reviews = reportingService.getFilteredReview(request);
	        return ResponseEntity.ok(reviews);
	}
	 
	 @PostMapping("/review-report")
	    public ResponseEntity<ReviewReportingDataPageResponse> getReviewReportingData(@RequestBody ReviewRequest request) {
	        ReviewReportingDataPageResponse response = reportingService.getReviewReportingData(request);
	        return ResponseEntity.ok(response);
	 }

	@PostMapping("/gmb-location-report")
	public ResponseEntity<GMBLocationReportPageResponse> getGMBLocationReportData(@RequestBody GMBLocationReportRequest request) {
		GMBLocationReportPageResponse response = reportingService.getGMBLocationReportData(request);
		return ResponseEntity.ok(response);
	}
}
