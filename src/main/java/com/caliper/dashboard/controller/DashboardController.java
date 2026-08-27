package com.caliper.dashboard.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.caliper.dashboard.dto.DashboardModules;
import com.caliper.dashboard.dto.DashboardRequestDTO;
import com.caliper.dashboard.dto.campaign.DashboardCampaign;
import com.caliper.dashboard.dto.insights.DashboardGoogleInsights;
import com.caliper.dashboard.dto.location.DashboardLocations;
import com.caliper.dashboard.dto.post.DashboardPosts;
import com.caliper.dashboard.dto.review.DashboardReviews;
import com.caliper.dashboard.dto.socialMediaInsights.DashboardSocialMediaInsights;
import com.caliper.dashboard.service.DashboardService;


@RestController
@RequestMapping("/dashboard")
public class DashboardController {


	@Autowired
	private DashboardService dashboardService;

	@PostMapping("/locations")
	public ResponseEntity<?> fetchDashboardLocationDetails(@RequestBody DashboardRequestDTO requestDTO) {

		DashboardLocations dashboardLocations = dashboardService.fetchDashboardLocationDetails(requestDTO);

		return ResponseEntity.ok(dashboardLocations);
	}

	@PostMapping("/campaigns")
	public ResponseEntity<?> fetchDashboardCampaignDetails(@RequestBody DashboardRequestDTO requestDTO) {

		if (!dashboardService.hasAccess(requestDTO, DashboardModules.CAMPAIGNS.name())) {
			throw new SecurityException("You do not have permission to access Campaigns");
		}
		DashboardCampaign dashboardCampaign = dashboardService.fetchDashboardCampaignDetails(requestDTO);

		return ResponseEntity.ok(dashboardCampaign);
	}

	@PostMapping("/reviews")
	public ResponseEntity<?> fetchDashboardReviewDetails(@RequestBody DashboardRequestDTO requestDTO
			) {

		if (!dashboardService.hasAccess(requestDTO, DashboardModules.REVIEWS.name())) {
			throw new SecurityException("You do not have permission to access Reviews");
		}
		DashboardReviews dashboardReviews = dashboardService.fetchDashboardReviewDetails(requestDTO);

		return ResponseEntity.ok(dashboardReviews);
	}

	@PostMapping("/insights")
	public ResponseEntity<?> fetchDashboardGoogleInsightDetails(@RequestBody DashboardRequestDTO requestDTO
			) {
		
		if (!dashboardService.hasAccess(requestDTO, DashboardModules.GMB_INSIGHTS.name())) {
			throw new SecurityException("You do not have permission to access Google Business Insights");
		}
		DashboardGoogleInsights dashboardGoogleInsights = dashboardService.fetchDashboardGoogleInsightDetails(requestDTO);

		return ResponseEntity.ok(dashboardGoogleInsights);
	}

	@PostMapping("/posts")
	public ResponseEntity<?> fetchDashboardPostsDetails(@RequestBody DashboardRequestDTO requestDTO) {

		if (!dashboardService.hasAccess(requestDTO, DashboardModules.POSTS.name())) {
			throw new SecurityException("You do not have permission to access Posts");
		}
		DashboardPosts dashboardPosts = dashboardService.fetchDashboardPostsDetails(requestDTO);

		return ResponseEntity.ok(dashboardPosts);
	}

	@PostMapping("/socialMediaInsights")
	public ResponseEntity<?> fetchDashboardSocialMediaInsights(@RequestBody DashboardRequestDTO requestDTO) {
		
		if (!dashboardService.hasAccess(requestDTO, DashboardModules.SOCIAL_MEDIA_INSIGHTS.name())) {
			throw new SecurityException("You do not have permission to access Social Media Insights");
		}
		DashboardSocialMediaInsights dashboardSocialMediaInsights = dashboardService.fetchDashboardSocialMediaInsightsDetails(requestDTO);

		return ResponseEntity.ok(dashboardSocialMediaInsights);
	}
}
