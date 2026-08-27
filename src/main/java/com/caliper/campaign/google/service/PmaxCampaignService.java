package com.caliper.campaign.google.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caliper.campaign.google.dto.request.COECampaignDetailsDto;
import com.caliper.campaign.google.dto.request.COEPmaxCampaignDetailsDto;
import com.caliper.campaign.google.dto.request.ClientPmaxCampaignDetailsDto;
import com.caliper.campaign.google.dto.response.SelfServeResponse;
import com.caliper.campaign.google.entity.ClientLocationSetup;
import com.caliper.campaign.google.entity.GoogleCampaign;
import com.caliper.campaign.google.entity.GoogleCampaignAssetGroup;
import com.caliper.campaign.google.entity.GoogleCampaignAssets;
import com.caliper.campaign.google.entity.GoogleCampaignGeoDetails;
import com.caliper.campaign.google.repository.ClientLocationSetupRepository;
import com.caliper.campaign.google.repository.GoogleCampaignAssetGroupRepository;
import com.caliper.campaign.google.repository.GoogleCampaignAssetsRepository;
import com.caliper.campaign.google.repository.GoogleCampaignGeoDetailsRepository;
import com.caliper.campaign.google.repository.GoogleCampaignRepository;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.repository.DealerLocationRepository;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.google.ads.googleads.v21.enums.AssetFieldTypeEnum.AssetFieldType;

@Service
public class PmaxCampaignService {

	@Autowired
	public GoogleCampaignRepository googleCampaignRepository;

	@Autowired
	public GoogleCampaignAssetGroupRepository googleCampaignAssetGroupRepository;

	@Autowired
	public GoogleCampaignAssetsRepository googleCampaignAssetsRepository;

	@Autowired
	public GoogleCampaignGeoDetailsRepository googleCampaignGeoDetailsRepository;

	@Autowired
	public ClientLocationSetupRepository clientLocationSetupRepository;
	
	@Autowired
	public DealerLocationRepository dealerLocationRepository;

	//coe post call
	@Transactional
	public SelfServeResponse createCoePmaxCampaign(COEPmaxCampaignDetailsDto request) {

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

		List<String> headLines = request.getHeadlines();
		List<String> descriptions = request.getDescriptions();
		List<String> longHeadlines = request.getLongHeadlines();

		Optional<GoogleCampaignAssetGroup> assetGroups = googleCampaignAssetGroupRepository.findByCampaignId(request.getCampaignId());
		GoogleCampaignAssetGroup assetGroup = assetGroups.orElseThrow(() -> new ResourceNotFoundException("Asset Group not found for campaign: " + request.getCampaignId()));
		long assetGroupId = assetGroup.getId();
		
		googleCampaignAssetsRepository.deleteByAssetGroupIdAndType(assetGroupId, AssetFieldType.HEADLINE.toString());
		googleCampaignAssetsRepository.deleteByAssetGroupIdAndType(assetGroupId, AssetFieldType.DESCRIPTION.toString());
		googleCampaignAssetsRepository.deleteByAssetGroupIdAndType(assetGroupId, AssetFieldType.LONG_HEADLINE.toString());

		if(headLines != null && headLines.size()>0) {
			for(String headLine : headLines) {
				GoogleCampaignAssets googleCampaignAssets = new GoogleCampaignAssets(0L, assetGroupId, "-1", "-1", assetGroup.getCampaignId(), AssetFieldType.HEADLINE.toString(), headLine, "");
				googleCampaignAssetsRepository.save(googleCampaignAssets);
			}
		}
		if(descriptions != null && descriptions.size()>0) {
			for(String description : descriptions) {
				GoogleCampaignAssets googleCampaignAssets = new GoogleCampaignAssets(0L, assetGroupId, "-1", "-1", assetGroup.getCampaignId(), AssetFieldType.DESCRIPTION.toString(), description, "");
				googleCampaignAssetsRepository.save(googleCampaignAssets);
			}
		}
		if(longHeadlines != null && longHeadlines.size()>0) {
			for(String longHeadline : longHeadlines) {
				GoogleCampaignAssets googleCampaignAssets = new GoogleCampaignAssets(0L, assetGroupId, "-1", "-1", assetGroup.getCampaignId(), AssetFieldType.LONG_HEADLINE.toString(), longHeadline, "");
				googleCampaignAssetsRepository.save(googleCampaignAssets);
			}
		} 	

		ClientLocationSetup clientLocationSetup = clientLocationSetupRepository.findByClientIdAndDealerId(campaign.getClientId(), campaign.getDealerId()).orElseThrow(() -> new ResourceNotFoundException("Client location setup not found"));
		DealerLocation location = dealerLocationRepository.getDealerLocationByDealerIdAndClientId(clientLocationSetup.getDealerId(), clientLocationSetup.getClientId());

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

		return new SelfServeResponse(SelfServeResponse.RESULT_SUCCESS, "coe pmax campaign created with id : "+request.getCampaignId(), GoogleCampaign.ROLE_HUB_USER, request.getCampaignId());
	}

	//client post call
	@Transactional()
	public SelfServeResponse createClientPmaxCampaignAction(ClientPmaxCampaignDetailsDto request){

		boolean existingCampaign = googleCampaignRepository.existsByCampaignNameAndClientId(request.getCampaignName(), request.getClientId());
		
		if(existingCampaign) {
			return new SelfServeResponse(SelfServeResponse.RESULT_FAILURE, "Client campaign name is duplicate", GoogleCampaign.CLIENT, 0l);
		}
		
		GoogleCampaign campaign  = GoogleCampaign.builder()
				.clientId(request.getClientId())
				.campaignName(request.getCampaignName())
				.campaignResourceName("-1")
				.platform(GoogleCampaign.PMAX_CAMPAIGN)
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

		GoogleCampaignAssetGroup assetGroup = GoogleCampaignAssetGroup.builder()
				.campaignId(campaignId)
				.assetGroupName(request.getCampaignName())
				.assetGroupResourceName("-1")
				.finalUrl(request.getLandingPageUrl())
				.mobileUrl(request.getLandingPageUrl())
				.build();

		googleCampaignAssetGroupRepository.save(assetGroup);
		long latestAssetGroupId = assetGroup.getId();

		List<String> headlines = request.getHeadlines();
		List<String> descriptions = request.getDescriptions();
		List<String> longHeadlines = request.getLongHeadlines();
		List<String> marketingImages = request.getMarketingImages();
		List<String> squareMarketingImages = request.getSquareMarketingImages();
		List<String> portraitMarketingImages = request.getPortraitMarketingImages();
		List<String> logos = request.getLogo();
		List<String> landscapeLogos = request.getLandscapeLogo();
		String businessName = request.getBusinessName();

		if(headlines != null && headlines.size()>0) {
			for(String headLine : headlines) {
				GoogleCampaignAssets campaignAssets = GoogleCampaignAssets.builder()
						.assetGroupId(latestAssetGroupId)
						.assetGroupAssetResourceName("-1")
						.assetResourceName("-1")
						.campaignID(campaignId)
						.type(AssetFieldType.HEADLINE.toString())
						.value(headLine)
						.imageFileName("").build();
				googleCampaignAssetsRepository.save(campaignAssets);
			}
		}

		if(descriptions != null && descriptions.size()>0) {
			for(String description : descriptions) {
				GoogleCampaignAssets campaignAssets = GoogleCampaignAssets.builder()
						.assetGroupId(latestAssetGroupId)
						.assetGroupAssetResourceName("-1")
						.assetResourceName("-1")
						.campaignID(campaignId)
						.type(AssetFieldType.DESCRIPTION.toString())
						.value(description)
						.imageFileName("").build();
				googleCampaignAssetsRepository.save(campaignAssets);
			}
		}

		if(longHeadlines != null && longHeadlines.size()>0) {
			for(String longHeadline : longHeadlines) {
				GoogleCampaignAssets campaignAssets = GoogleCampaignAssets.builder()
						.assetGroupId(latestAssetGroupId)
						.assetGroupAssetResourceName("-1")
						.assetResourceName("-1")
						.campaignID(campaignId)
						.type(AssetFieldType.LONG_HEADLINE.toString())
						.value(longHeadline)
						.imageFileName("").build();
				googleCampaignAssetsRepository.save(campaignAssets);
			}
		}

		if(marketingImages != null && marketingImages.size()>0) {
			for(String marketingImage : marketingImages) {
				GoogleCampaignAssets campaignAssets = GoogleCampaignAssets.builder()
						.assetGroupId(latestAssetGroupId)
						.assetGroupAssetResourceName("-1")
						.assetResourceName("-1")
						.campaignID(campaignId)
						.type(AssetFieldType.MARKETING_IMAGE.toString())
						.value(marketingImage)
						.imageFileName("").build();
				googleCampaignAssetsRepository.save(campaignAssets);
			}
		}

		if(squareMarketingImages != null && squareMarketingImages.size()>0) {
			for(String squareMarketingImage : squareMarketingImages) {
				GoogleCampaignAssets campaignAssets = GoogleCampaignAssets.builder()
						.assetGroupId(latestAssetGroupId)
						.assetGroupAssetResourceName("-1")
						.assetResourceName("-1")
						.campaignID(campaignId)
						.type(AssetFieldType.SQUARE_MARKETING_IMAGE.toString())
						.value(squareMarketingImage)
						.imageFileName("").build();
				googleCampaignAssetsRepository.save(campaignAssets);
			}
		}

		if(portraitMarketingImages != null && portraitMarketingImages.size()>0) {
			for(String portraitMarketingImage : portraitMarketingImages) {
				GoogleCampaignAssets campaignAssets = GoogleCampaignAssets.builder()
						.assetGroupId(latestAssetGroupId)
						.assetGroupAssetResourceName("-1")
						.assetResourceName("-1")
						.campaignID(campaignId)
						.type(AssetFieldType.PORTRAIT_MARKETING_IMAGE.toString())
						.value(portraitMarketingImage)
						.imageFileName("").build();
				googleCampaignAssetsRepository.save(campaignAssets);
			}
		}

		if(logos != null && logos.size()>0) {
			for(String logo : logos) {
				GoogleCampaignAssets campaignAssets = GoogleCampaignAssets.builder()
						.assetGroupId(latestAssetGroupId)
						.assetGroupAssetResourceName("-1")
						.assetResourceName("-1")
						.campaignID(campaignId)
						.type(AssetFieldType.LOGO.toString())
						.value(logo)
						.imageFileName("").build();
				googleCampaignAssetsRepository.save(campaignAssets);
			}
		}

		if(landscapeLogos != null && landscapeLogos.size()>0) {
			for(String landscapeLogo : landscapeLogos) {
				GoogleCampaignAssets campaignAssets = GoogleCampaignAssets.builder()
						.assetGroupId(latestAssetGroupId)
						.assetGroupAssetResourceName("-1")
						.assetResourceName("-1")
						.campaignID(campaignId)
						.type(AssetFieldType.LANDSCAPE_LOGO.toString())
						.value(landscapeLogo)
						.imageFileName("").build();
				googleCampaignAssetsRepository.save(campaignAssets);
			}
		}

		if(!businessName.isEmpty()) {
				GoogleCampaignAssets campaignAssets = GoogleCampaignAssets.builder()
						.assetGroupId(latestAssetGroupId)
						.assetGroupAssetResourceName("-1")
						.assetResourceName("-1")
						.campaignID(campaignId)
						.type(AssetFieldType.BUSINESS_NAME.toString())
						.value(businessName)
						.imageFileName("").build();
				googleCampaignAssetsRepository.save(campaignAssets);
		}
		
		return new SelfServeResponse(SelfServeResponse.RESULT_SUCCESS, "client search campaign created with id : " +campaignId, GoogleCampaign.CLIENT, campaignId);
	}
}
