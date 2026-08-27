package com.caliper.campaign.google.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.campaign.google.dto.request.COECampaignDetailsDto;
import com.caliper.campaign.google.dto.request.ClientSearchCampaignDetailsDto;
import com.caliper.campaign.google.dto.response.SelfServeResponse;
import com.caliper.campaign.google.service.SearchCampaignService;

@RestController
@RequestMapping("/search-campaign")
public class SearchCampaignController {

	@Autowired
	public SearchCampaignService searchCampaignService;
	
	@PostMapping("/client-campaign")
	public ResponseEntity<SelfServeResponse> insertClientCampaign(@RequestBody ClientSearchCampaignDetailsDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(searchCampaignService.createClientSearchCampaignAction(dto));
	}
	
	@PostMapping("/coe-campaign")
	public ResponseEntity<SelfServeResponse> insertCOECampaign(@RequestBody COECampaignDetailsDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(searchCampaignService.createCoeSearchCampaign(dto));
	}
	
}
