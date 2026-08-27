package com.caliper.campaign.google.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.caliper.campaign.google.dto.request.CampaignFilterRequest;
import com.caliper.campaign.google.dto.request.CampaignReportActionRequest;
import com.caliper.campaign.google.dto.request.ClientAccountSetupDto;
import com.caliper.campaign.google.dto.request.ClientOnboardingDetailsDto;
import com.caliper.campaign.google.dto.response.CampaignApiResponse;
import com.caliper.campaign.google.dto.response.CampaignReportResponse;
import com.caliper.campaign.google.dto.response.IndustryDetailsDto;
import com.caliper.campaign.google.dto.response.ClientLocationDetailsDto;
import com.caliper.campaign.google.dto.response.PopulateClientKeywordDetailsDto;
import com.caliper.campaign.google.dto.response.PopulateClientLocationDetailsDto;
import com.caliper.campaign.google.dto.response.PopulateClientOnboardingDetailsDto;
import com.caliper.campaign.google.dto.response.SelfServeResponse;
import com.caliper.campaign.google.dto.response.ViewCampaignSetupDetailsResponse;
import com.caliper.campaign.google.dto.response.ViewPageableCampaignResponse;
import com.caliper.campaign.google.service.CampaignReportService;
import com.caliper.campaign.google.service.CampaignService;
import com.caliper.onboarding.service.OnboardingService;
import com.google.cloud.bigquery.JobException;

@RestController
@RequestMapping("/campaign")
public class CampaignController {

	@Autowired
	public CampaignService campaignService;

	@Autowired
	public CampaignReportService campaignReportService;

	@Autowired
	private OnboardingService onboardingService;

	//post call client onboarding  : client setup screen
	@PostMapping("/client-setup")
	public ResponseEntity<CampaignApiResponse> insertClientDetails(@RequestBody ClientAccountSetupDto dto) throws InterruptedException {
		campaignService.insertClientDetails(dto);
		onboardingService.completeCampaignSetup(dto.getClientId());
		String nextStep = onboardingService.getCurrentStepName(dto.getClientId());
		return ResponseEntity.status(HttpStatus.CREATED).body(CampaignApiResponse.builder()
				.success(true)
				.message("Client setup completed successfully")
				.clientId(dto.getClientId())
				.onboardingStep(nextStep)
				.build());
	}

	//get call for client onboarding  : client setup screen
	@GetMapping("/{clientId}/details")
	public ResponseEntity<PopulateClientOnboardingDetailsDto> getClientOnboardingDetails(@PathVariable("clientId") String clientId, @RequestParam("userId") String userId) {
		return ResponseEntity.ok(campaignService.getClientOnboardingDetails(clientId, userId));
	}

	//get call for client location details : add details of location screen - CASE 1
	@GetMapping("/{clientId}/location")
	public ResponseEntity<ClientLocationDetailsDto> getLocationDetails(@PathVariable("clientId") String clientId, @RequestParam("dealerId") String dealerId){
		return ResponseEntity.ok(campaignService.getLocationDetails(clientId, dealerId));
	}
	
	
	//get call for client campaign details : campaign setup screen
	@GetMapping("/{clientId}/locations")
	public ResponseEntity<PopulateClientLocationDetailsDto> getClientLocationDetails(@PathVariable String clientId, @RequestParam String userId) {
		return ResponseEntity.ok(campaignService.getClientLocationDetails(clientId, userId));
	}

	//getCall for all client campaigns : all campaigns screen
	@PostMapping("/all-campaigns")
	public ResponseEntity<ViewPageableCampaignResponse> getViewAllCampaignsResponse(@RequestBody CampaignFilterRequest request) {
		return ResponseEntity.ok(campaignService.viewAllCampaigns(request));
	}

	//get call for internal client onboarding : internal team screen
	@GetMapping("/coe/onboarding-details")
	public ResponseEntity<PopulateClientKeywordDetailsDto> getClientKeywordsDetails() {
		return ResponseEntity.ok(campaignService.getClientKeywordDetails());
	}

	//post call for internal client onbording : internal team screen
	@PostMapping("/coe/client-account-setup")
	public ResponseEntity<CampaignApiResponse> insertGoogleAccountAndClientDataSetupKeyword(@RequestBody ClientOnboardingDetailsDto dto) {
		campaignService.insertGoogleAccountAndClientDataSetupKeyword(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(CampaignApiResponse.builder()
				.success(true)
				.message("Client account setup completed successfully")
				.clientId(dto.getClientId())
				.build());
	}

	//coe view all campaigns
	@GetMapping("/coe/all-campaigns")
	public ResponseEntity<ViewPageableCampaignResponse> getViewAllCoeCampaignsResponse(@RequestParam String search, @RequestParam int pageNo) {
		return ResponseEntity.ok(campaignService.viewAllCoeCampaigns(search, pageNo));
	}

	//coe view all campaigns
	@GetMapping("/coe/campaign-setup-details/{campaignId}")
	public ResponseEntity<ViewCampaignSetupDetailsResponse> getCampaignSetupDetails(@PathVariable Long campaignId) {
		return ResponseEntity.ok(campaignService.viewCampaignSetupDetails(campaignId));
	}
		
	//post call for campaign report : client campaign report screen 
	@PostMapping("/campaign-report")
	public CampaignReportResponse getCampaignReport(@RequestBody CampaignReportActionRequest parsedRequest) throws FileNotFoundException, JobException, SQLException, ParseException, IOException, InterruptedException {
		return campaignReportService.process(parsedRequest);
	}

	@PostMapping("/pause-campaign/{campaignId}")
	public ResponseEntity<SelfServeResponse> pauseCampaign(@PathVariable Long campaignId, @RequestParam String clientId, @RequestParam String userId, @RequestParam String comment) {
		SelfServeResponse response = campaignService.pauseCampaign(clientId, userId, campaignId, comment);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/resume-campaign/{campaignId}")
	public ResponseEntity<SelfServeResponse> resumeCampaign(@PathVariable Long campaignId, @RequestParam String clientId, @RequestParam String userId, @RequestParam String comment) {
		SelfServeResponse response = campaignService.resumeCampaign(clientId, userId, campaignId, comment);
		return ResponseEntity.ok(response);
	}

	//Get all Industry and respective sub industry details
	
	@GetMapping("/industries")
	public ResponseEntity<IndustryDetailsDto> getIndustryDetails() {
		return ResponseEntity.ok(campaignService.getIndustryDetails());
	}

}
