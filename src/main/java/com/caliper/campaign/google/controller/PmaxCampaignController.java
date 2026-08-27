package com.caliper.campaign.google.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.campaign.google.dto.request.COEPmaxCampaignDetailsDto;
import com.caliper.campaign.google.dto.request.ClientPmaxCampaignDetailsDto;
import com.caliper.campaign.google.dto.response.SelfServeResponse;
import com.caliper.campaign.google.service.PmaxCampaignService;

@RestController
@RequestMapping("/pmax-campaign")
public class PmaxCampaignController {

	@Autowired
	public PmaxCampaignService pmaxCampaignService;
	
	@PostMapping("/client-campaign")
	public ResponseEntity<SelfServeResponse> insertClientCampaign(@RequestBody ClientPmaxCampaignDetailsDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(pmaxCampaignService.createClientPmaxCampaignAction(dto));
	}
	
	@PostMapping("/coe-campaign")
	public ResponseEntity<SelfServeResponse> insertCOECampaign(@RequestBody COEPmaxCampaignDetailsDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(pmaxCampaignService.createCoePmaxCampaign(dto));
	}
	
	
}
