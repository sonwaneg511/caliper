
package com.caliper.campaign.google.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.campaign.google.dto.request.COECampaignDetailsDto;
import com.caliper.campaign.google.dto.request.ClientCallAdCampaignDetailsDto;
import com.caliper.campaign.google.dto.response.SelfServeResponse;
import com.caliper.campaign.google.service.CallAdCampaignService;

@RestController
@RequestMapping("/call-ad-campaign")
public class CallAdCampaignController {

	@Autowired
	public CallAdCampaignService callAdCampaignService;
	
	@PostMapping("/client-campaign")
	public ResponseEntity<SelfServeResponse> insertClientCampaign(@RequestBody ClientCallAdCampaignDetailsDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(callAdCampaignService.createClientCallAdCampaignAction(dto));
	}
	
	@PostMapping("/coe-campaign")
	public ResponseEntity<SelfServeResponse> insertCOECampaign(@RequestBody COECampaignDetailsDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(callAdCampaignService.createCoeCallAdCampaign(dto));
	}
}
	