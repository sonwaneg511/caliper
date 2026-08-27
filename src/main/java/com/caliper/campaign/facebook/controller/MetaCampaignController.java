package com.caliper.campaign.facebook.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.caliper.campaign.facebook.dto.request.ClientMetaCampaignDetailsDto;
import com.caliper.campaign.facebook.dto.request.CoeMetaCampaignDetailsDto;
import com.caliper.campaign.facebook.dto.request.MetaCampaignFilterRequest;
import com.caliper.campaign.facebook.dto.request.MetaCampaignReportRequest;
import com.caliper.campaign.facebook.dto.response.MetaCampaignReportResponse;
import com.caliper.campaign.facebook.dto.response.ViewMetaCampaignDetailsResponse;
import com.caliper.campaign.facebook.dto.response.ViewMetaCampaignResponse;
import com.caliper.campaign.facebook.service.MetaCampaignService;
import com.caliper.campaign.facebook.service.MetaInsightsService;
import com.caliper.campaign.facebook.service.MetaWebhookService;
import com.caliper.campaign.google.dto.response.SelfServeResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/meta-campaign")
public class MetaCampaignController {

    @Autowired
    private MetaCampaignService metaCampaignService;

    @Autowired
    private MetaInsightsService metaInsightsService;

    @Autowired
    private MetaWebhookService metaWebhookService;

    @PostMapping("/client-campaign")
    public ResponseEntity<SelfServeResponse> createClientMetaCampaign(
            @Valid @RequestBody ClientMetaCampaignDetailsDto request) {
        return ResponseEntity.ok(metaCampaignService.createClientMetaCampaign(request));
    }

    @PostMapping("/coe-approve")
    public ResponseEntity<SelfServeResponse> approveCampaign(
            @Valid @RequestBody CoeMetaCampaignDetailsDto request) {
        return ResponseEntity.ok(metaCampaignService.approveCampaign(request));
    }

    @PostMapping("/all-campaigns")
    public ResponseEntity<Page<ViewMetaCampaignResponse>> viewAllCampaigns(
            @RequestBody MetaCampaignFilterRequest request) {
        return ResponseEntity.ok(metaCampaignService.viewAllCampaigns(request));
    }

    @GetMapping("/campaign-details/{campaignId}")
    public ResponseEntity<ViewMetaCampaignDetailsResponse> getCampaignDetails(
            @PathVariable Long campaignId) {
        return ResponseEntity.ok(metaCampaignService.getCampaignDetails(campaignId));
    }

    @PostMapping("/pause/{campaignId}")
    public ResponseEntity<SelfServeResponse> pauseCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(
                new SelfServeResponse(SelfServeResponse.RESULT_SUCCESS,
                        "Pause initiated for campaign: " + campaignId,
                        null, campaignId));
    }

    @PostMapping("/resume/{campaignId}")
    public ResponseEntity<SelfServeResponse> resumeCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(
                new SelfServeResponse(SelfServeResponse.RESULT_SUCCESS,
                        "Resume initiated for campaign: " + campaignId,
                        null, campaignId));
    }

    @GetMapping("/objective-options")
    public ResponseEntity<Map<String, Object>> getObjectiveOptions() {
        return ResponseEntity.ok(metaCampaignService.getObjectiveOptions());
    }

    @PostMapping("/campaign-report")
    public ResponseEntity<MetaCampaignReportResponse> getCampaignReport(
            @Valid @RequestBody MetaCampaignReportRequest request) {
        return ResponseEntity.ok(metaInsightsService.getCampaignInsights(
                request.getCampaignId(),
                request.getDatePreset()));
    }

    @GetMapping("/geo-search")
    public ResponseEntity<List<Map<String, String>>> searchGeoLocations(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "city") String location_type) {
        return ResponseEntity.ok(metaCampaignService.searchGeoLocations(null, q, location_type));
    }

    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        String response = metaWebhookService.verifyWebhook(mode, token, challenge);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {
        metaWebhookService.processLeadEvent(payload, signature);
        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
