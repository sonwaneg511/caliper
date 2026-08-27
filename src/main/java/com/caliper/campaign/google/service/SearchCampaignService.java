package com.caliper.campaign.google.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caliper.campaign.google.dto.request.COECampaignDetailsDto;
import com.caliper.campaign.google.dto.request.ClientSearchCampaignDetailsDto;
import com.caliper.campaign.google.dto.response.SelfServeResponse;
import com.caliper.campaign.google.entity.ClientAccountSetup;
import com.caliper.campaign.google.entity.ClientLocationSetup;
import com.caliper.campaign.google.entity.GoogleAdgroup;
import com.caliper.campaign.google.repository.ClientAccountSetupRepository;
import com.caliper.campaign.google.entity.GoogleCampaign;
import com.caliper.campaign.google.entity.GoogleCampaignGeoDetails;
import com.caliper.campaign.google.entity.GoogleKeyword;
import com.caliper.campaign.google.entity.GoogleResponsiveAd;
import com.caliper.campaign.google.repository.ClientLocationSetupRepository;
import com.caliper.campaign.google.repository.GoogleAdgroupRepository;
import com.caliper.campaign.google.repository.GoogleCampaignGeoDetailsRepository;
import com.caliper.campaign.google.repository.GoogleCampaignRepository;
import com.caliper.campaign.google.repository.GoogleKeywordRepository;
import com.caliper.campaign.google.repository.GoogleResponsiveAdRepository;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.repository.DealerLocationRepository;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;

@Service
public class SearchCampaignService {

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
	public ClientLocationSetupRepository clientLocationSetupRepository;
	
	@Autowired
	public DealerLocationRepository dealerLocationRepository;

	@Autowired
	public ClientAccountSetupRepository clientAccountSetupRepository;

	//coe post call
	@Transactional
	public SelfServeResponse createCoeSearchCampaign(COECampaignDetailsDto request) {

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

		googleResponsiveAdRepository.deleteByAdgroupIdAndType(adGroupId, GoogleResponsiveAd.HEADLINE);
		googleResponsiveAdRepository.deleteByAdgroupIdAndType(adGroupId, GoogleResponsiveAd.DESCRIPTION);

		if(headlines != null && headlines.size()>0) {
			for(String headline : headlines) {
				GoogleResponsiveAd googleResponsiveAd = new GoogleResponsiveAd(0L, request.getClientId(), adGroupId, "-1", GoogleResponsiveAd.HEADLINE, headline, GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAUSED);
				googleResponsiveAdRepository.save(googleResponsiveAd);
			}
		}

		if(descriptions != null && descriptions.size()>0) {
			for(String description : descriptions) {
				GoogleResponsiveAd googleResponsiveAd = new GoogleResponsiveAd(0L,  request.getClientId(), adGroupId, "-1", GoogleResponsiveAd.DESCRIPTION, description, GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAUSED);
				googleResponsiveAdRepository.save(googleResponsiveAd);
			}
		}

		if(request.getAdName() != null) {
			GoogleResponsiveAd adNameResponsiveAd = new GoogleResponsiveAd(0L, request.getClientId(), adGroupId, "-1", GoogleResponsiveAd.AD_NAME, request.getAdName(), GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAUSED);
			googleResponsiveAdRepository.save(adNameResponsiveAd);
		}
		
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

		return new SelfServeResponse(SelfServeResponse.RESULT_SUCCESS, "coe search campaign created with id : "+request.getCampaignId(), GoogleCampaign.ROLE_HUB_USER, request.getCampaignId());
	}

	//client post call
	@Transactional
	public SelfServeResponse createClientSearchCampaignAction(ClientSearchCampaignDetailsDto request){

		boolean existingCampaign = googleCampaignRepository.existsByCampaignNameAndClientId(request.getCampaignName(), request.getClientId());
		
		if(existingCampaign) {
			return new SelfServeResponse(SelfServeResponse.RESULT_FAILURE, "Client campaign name is duplicate", GoogleCampaign.CLIENT, 0l);
		}
		
		GoogleCampaign campaign  = GoogleCampaign.builder()
				.clientId(request.getClientId())
				.campaignName(request.getCampaignName())
				.campaignResourceName("-1")
				.platform(GoogleCampaign.SEARCH_CAMPAIGN)
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

		List<String> headlines = request.getHeadlines();
		List<String> descriptions = request.getDescriptions();

		if(headlines != null && headlines.size()>0) {
			for(String headline : headlines) {
				GoogleResponsiveAd googleResponsiveAd = GoogleResponsiveAd.builder()
						.clientId(request.getClientId())
						.adgroupId(latestAdGroupId)
						.adResourceName("-1")
						.type(GoogleResponsiveAd.HEADLINE)
						.value(headline)
						.status(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAUSED).build();
				googleResponsiveAdRepository.save(googleResponsiveAd);
			}
		}

		if(descriptions != null && descriptions.size()>0) {
			for(String description : descriptions) {
				GoogleResponsiveAd googleResponsiveAd = GoogleResponsiveAd.builder()
						.clientId(request.getClientId())
						.adgroupId(latestAdGroupId)
						.adResourceName("-1")
						.type(GoogleResponsiveAd.DESCRIPTION)
						.value(description)
						.status(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAUSED).build();
				googleResponsiveAdRepository.save(googleResponsiveAd);
			}
		}

		if(request.getLandingPageUrl() != null) {
			GoogleResponsiveAd googleResponsiveAd = GoogleResponsiveAd.builder()
					.clientId(request.getClientId())
					.adgroupId(latestAdGroupId)
					.adResourceName("-1")
					.type(GoogleResponsiveAd.FINAL_URL)
					.value(request.getLandingPageUrl())
					.status(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAUSED).build();
			googleResponsiveAdRepository.save(googleResponsiveAd);
		}

		if(request.getLandingPageUrl() != null) {
			GoogleResponsiveAd googleResponsiveAd1 = GoogleResponsiveAd.builder()
					.clientId(request.getClientId())
					.adgroupId(latestAdGroupId)
					.adResourceName("-1")
					.type(GoogleResponsiveAd.DISPLAY_URL)
					.value(request.getLandingPageUrl())
					.status(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAUSED).build();
			googleResponsiveAdRepository.save(googleResponsiveAd1);
		}

		return new SelfServeResponse(SelfServeResponse.RESULT_SUCCESS, "client search campaign created with id : " +campaignId, GoogleCampaign.CLIENT, campaignId);
	}


}
