package com.caliper.campaign.google.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caliper.campaign.google.dto.request.COECampaignDetailsDto;
import com.caliper.campaign.google.dto.request.ClientCallAdCampaignDetailsDto;
import com.caliper.campaign.google.dto.response.SelfServeResponse;
import com.caliper.campaign.google.entity.ClientAccountSetup;
import com.caliper.campaign.google.entity.ClientLocationSetup;
import com.caliper.campaign.google.entity.GoogleAdgroup;
import com.caliper.campaign.google.repository.ClientAccountSetupRepository;
import com.caliper.campaign.google.entity.GoogleCallAd;
import com.caliper.campaign.google.entity.GoogleCampaign;
import com.caliper.campaign.google.entity.GoogleCampaignGeoDetails;
import com.caliper.campaign.google.entity.GoogleKeyword;
import com.caliper.campaign.google.repository.ClientLocationSetupRepository;
import com.caliper.campaign.google.repository.GoogleAdgroupRepository;
import com.caliper.campaign.google.repository.GoogleCallAdRepository;
import com.caliper.campaign.google.repository.GoogleCampaignGeoDetailsRepository;
import com.caliper.campaign.google.repository.GoogleCampaignRepository;
import com.caliper.campaign.google.repository.GoogleKeywordRepository;
import com.caliper.campaign.google.repository.GoogleResponsiveAdRepository;
import com.caliper.location.entity.Client;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.repository.ClientRepository;
import com.caliper.location.repository.DealerLocationRepository;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;

@Service
public class CallAdCampaignService {

	@Autowired
	public GoogleCampaignRepository googleCampaignRepository;

	@Autowired
	public GoogleAdgroupRepository googleAdgroupRepository;

	@Autowired
	public GoogleResponsiveAdRepository googleResponsiveAdRepository;

	@Autowired
	public GoogleCampaignGeoDetailsRepository googleCampaignGeoDetailsRepository;

	@Autowired
	public GoogleKeywordRepository googleKeywordRepository;

	@Autowired
	public GoogleCallAdRepository googleCallAdRepository;

	@Autowired
	public ClientLocationSetupRepository clientLocationSetupRepository;

	@Autowired
	public ClientRepository clientRepository;

	@Autowired
	public DealerLocationRepository dealerLocationRepository;

	@Autowired
	public ClientAccountSetupRepository clientAccountSetupRepository;

	//coe post call
	@Transactional
	public SelfServeResponse createCoeCallAdCampaign(COECampaignDetailsDto request) {

		GoogleCampaign campaign = googleCampaignRepository.findById(request.getCampaignId()).orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + request.getCampaignId()));

		campaign.setBiddingStrategy(request.getBiddingStrategy());
		campaign.setBiddingValue(request.getBiddingValue());
		campaign.setTargetGoogleSearch(request.isTargetGoogleSearch());
		campaign.setTargetSearchNetwork(request.isTargetSearchNetwork());
		campaign.setTargetContentNetwork(request.isTargetContentNetwork());
		campaign.setTargetPartnerSearchNetwork(request.isTargetPartnerSearchNetwork());
		campaign.setLastModidfiedDate(new Date());
		campaign.setStatus(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PENDING_DEPLOYMENT);
		campaign.setRadiusTarget(true);
		campaign.setPincodeTarget(false);

		googleCampaignRepository.save(campaign);

		GoogleAdgroup adGroup = googleAdgroupRepository.findFirstByCampaignId(request.getCampaignId()).orElseThrow(() ->
		new ResourceNotFoundException("AdGroup not found for campaignId: " + request.getCampaignId()));

		long adGroupId = adGroup.getId();

		List<String> headlines = request.getHeadlines();
		List<String> descriptions = request.getDescriptions();

		GoogleCallAd callAd = googleCallAdRepository.findByAdGroupId(adGroupId).orElseThrow(() -> new ResourceNotFoundException("Call ad not found for ad group: " + adGroupId));

		if (headlines != null && headlines.size() >= 2) {
			callAd.setHeadline1(headlines.get(0));
			callAd.setHeadline2(headlines.get(1));
		}
		if (descriptions != null && descriptions.size() >= 2) {
			callAd.setDescription1(descriptions.get(0));
			callAd.setDescription2(descriptions.get(1));
		}
		googleCallAdRepository.save(callAd);

		ClientLocationSetup clientLocationSetup = clientLocationSetupRepository.findByClientIdAndDealerId(campaign.getClientId(), campaign.getDealerId()).orElseThrow(() -> new ResourceNotFoundException("Client location setup not found"));
		DealerLocation location = dealerLocationRepository.getDealerLocationByDealerIdAndClientId(clientLocationSetup.getDealerId(), clientLocationSetup.getClientId());
		if (location == null) {
			throw new ResourceNotFoundException("Dealer location not found for dealer: " + clientLocationSetup.getDealerId());
		}

		GoogleCampaignGeoDetails googleCampaignGeoDetails1 = new GoogleCampaignGeoDetails(0L, request.getCampaignId(),  COECampaignDetailsDto.RADIUS, 0L, String.valueOf(clientLocationSetup.getRadius()), "-1");
		googleCampaignGeoDetailsRepository.save(googleCampaignGeoDetails1);

		GoogleCampaignGeoDetails googleCampaignGeoDetails2 = new GoogleCampaignGeoDetails(0L, request.getCampaignId(),  COECampaignDetailsDto.RADIUS_UNIT, 0L, clientLocationSetup.getRadiusUnit(), "-1");
		googleCampaignGeoDetailsRepository.save(googleCampaignGeoDetails2);

		GoogleCampaignGeoDetails googleCampaignGeoDetails3 = new GoogleCampaignGeoDetails(0L, request.getCampaignId(),  COECampaignDetailsDto.LATITUDE, 0L, clientLocationSetup.getLatitude(), "-1");
		googleCampaignGeoDetailsRepository.save(googleCampaignGeoDetails3);

		GoogleCampaignGeoDetails googleCampaignGeoDetails4 = new GoogleCampaignGeoDetails(0L, request.getCampaignId(),  COECampaignDetailsDto.LONGITUDE, 0L, clientLocationSetup.getLongitude(), "-1");
		googleCampaignGeoDetailsRepository.save(googleCampaignGeoDetails4);

		GoogleCampaignGeoDetails googleCampaignGeoDetails5 = new GoogleCampaignGeoDetails(0L, request.getCampaignId(),  COECampaignDetailsDto.COUNTRY,  0L, location.getCountry(), "-1");
		googleCampaignGeoDetailsRepository.save(googleCampaignGeoDetails5);
		
		List<String> keywordsList = request.getKeywords();
		if(keywordsList != null && !keywordsList.isEmpty()) {
			for(String keyword : keywordsList) {
				GoogleKeyword googleKeyword = new GoogleKeyword(0L, request.getClientId(), adGroupId, keyword, "-1", request.getMatchType());
				googleKeywordRepository.save(googleKeyword); 
			}
		}
		return new SelfServeResponse(SelfServeResponse.RESULT_SUCCESS, "coe call ad campaign created with id : " +request.getCampaignId(), GoogleCampaign.ROLE_HUB_USER, request.getCampaignId());
	}

	//client post call
	@Transactional
	public SelfServeResponse createClientCallAdCampaignAction(ClientCallAdCampaignDetailsDto request){

		boolean existingCampaign = googleCampaignRepository.existsByCampaignNameAndClientId(request.getCampaignName(), request.getClientId());
		
		if(existingCampaign) {
			return new SelfServeResponse(SelfServeResponse.RESULT_FAILURE, "Client campaign name is duplicate", GoogleCampaign.CLIENT, 0l);
		}
		
		GoogleCampaign campaign  = GoogleCampaign.builder()
				.clientId(request.getClientId())
				.campaignName(request.getCampaignName())
				.campaignResourceName("-1")
				.platform(GoogleCampaign.CALL_AD_CAMPAIGN)
				.googleAccountID("")
				.dealerId(request.getDealerId())
				.dailyBudget(request.getDailyBudget())
				.totalBudget(request.getTotalBudget())
				.budgetResourceName("-1")
				.campaignStructureType("")
				.campaignStructureSubType("")
				.model("")
				.isPortfolioBiddingStrategy(false)
				.biddingStrategy("")
				.biddingValue("")
				.isTargetGoogleSearch(false)
				.isTargetSearchNetwork(false)
				.isTargetContentNetwork(false)
				.isTargetPartnerSearchNetwork(false)
				.startDate(request.getStartDate())
				.endDate(request.getEndDate())
				.lastModidfiedDate(new Date())
				.lastModidfiedBy("USER")
				.isAdScheduled(false)
				.callExtension(false)
				.callAssetResourceName("-1")
				.callAssociationResourceName("-1")
				.locationInfoResourceName("-1")
				.status(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAYMENT_PENDING)
				.comment("")
				.clientComment(request.getClientComment())
				.clientObjective("")
				.isRadiusTarget(false)
				.isPincodeTarget(false)
				.build();

		googleCampaignRepository.save(campaign);
		long campaignId = campaign.getId();
		ClientAccountSetup clientAccountSetup = clientAccountSetupRepository.findByClientId(request.getClientId())
				.orElseThrow(() -> new ResourceNotFoundException("Client account setup not found for client: " + request.getClientId()));
		String cpcBid = String.valueOf(clientAccountSetup.getCpcBid());
		GoogleAdgroup googleAdgroup = GoogleAdgroup.builder()
				.adGroupName("ALL")
				.campaignId(campaignId)
				.adGroupResourceName("-1")
				.cpcBid(cpcBid).build();

		googleAdgroupRepository.save(googleAdgroup);
		long latestAdGroupId = googleAdgroup.getId();

		List<String> headLines = request.getHeadlines();
		List<String> descriptions = request.getDescriptions();
		
		String path1 = request.getPath1();
		String path2 = request.getPath2();
		String headLine1 = (headLines != null && headLines.size() > 0) ? headLines.get(0) : "";
		String headLine2 = (headLines != null && headLines.size() > 1) ? headLines.get(1) : "";
		String description1 = (descriptions != null && descriptions.size() > 0) ? descriptions.get(0) : "";
		String description2 = (descriptions != null && descriptions.size() > 1) ? descriptions.get(1) : "";
		String phoneNumber = "";

		Optional<ClientLocationSetup> locationSetupOpt = clientLocationSetupRepository.findByClientIdAndDealerId(request.getClientId(), request.getDealerId());

	    if(locationSetupOpt.isPresent()) {
			phoneNumber = locationSetupOpt.get().getAdPhoneNumber();
		}
		
		Client client = clientRepository.findByClientId(request.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found: " + request.getClientId()));
		String clientName = client.getClientName();

		GoogleCallAd googleCallAd = GoogleCallAd.builder()
				.clientId(request.getClientId())
				.adGroupId(latestAdGroupId)
				.adResourceName("-1")
				.businessName(clientName)
				.headline1(headLine1)
				.headline2(headLine2)
				.description1(description1)
				.description2(description2)
				.phoneNumber(phoneNumber)
				.verificationUrl(request.getLandingPageUrl())
				.finalUrl(request.getLandingPageUrl())
				.path1(path1)
				.path2(path2).build();

		googleCallAdRepository.save(googleCallAd);

		return new SelfServeResponse(SelfServeResponse.RESULT_SUCCESS, "client call ad campaign created with id  : " + campaignId, GoogleCampaign.CLIENT, campaignId);
	}


}

