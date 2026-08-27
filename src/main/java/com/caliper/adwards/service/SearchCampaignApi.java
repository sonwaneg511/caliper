package com.caliper.adwards.service;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.caliper.adwards.service.bidding.SEMBiddingStrategy;
import com.caliper.campaign.google.entity.GoogleCallAd;
import com.caliper.campaign.google.entity.GoogleCampaign;
import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.lib.utils.FieldMasks;
import com.google.ads.googleads.v21.common.AdTextAsset;
import com.google.ads.googleads.v21.common.AudienceInfo;
import com.google.ads.googleads.v21.common.CallAdInfo;
import com.google.ads.googleads.v21.common.GeoPointInfo;
import com.google.ads.googleads.v21.common.ImageAsset;
import com.google.ads.googleads.v21.common.KeywordInfo;
import com.google.ads.googleads.v21.common.MaximizeConversions;
import com.google.ads.googleads.v21.common.ProximityInfo;
import com.google.ads.googleads.v21.common.ResponsiveSearchAdInfo;
import com.google.ads.googleads.v21.common.TargetCpa;
import com.google.ads.googleads.v21.common.TargetRoas;
import com.google.ads.googleads.v21.common.TargetSpend;
import com.google.ads.googleads.v21.common.TextAsset;
import com.google.ads.googleads.v21.common.YoutubeVideoAsset;
import com.google.ads.googleads.v21.enums.AdGroupAdStatusEnum.AdGroupAdStatus;
import com.google.ads.googleads.v21.enums.AdGroupCriterionStatusEnum.AdGroupCriterionStatus;
import com.google.ads.googleads.v21.enums.AdGroupStatusEnum.AdGroupStatus;
import com.google.ads.googleads.v21.enums.AdGroupTypeEnum.AdGroupType;
import com.google.ads.googleads.v21.enums.AdvertisingChannelTypeEnum.AdvertisingChannelType;
import com.google.ads.googleads.v21.enums.AssetFieldTypeEnum.AssetFieldType;
import com.google.ads.googleads.v21.enums.AssetGroupStatusEnum.AssetGroupStatus;
import com.google.ads.googleads.v21.enums.CampaignStatusEnum.CampaignStatus;
import com.google.ads.googleads.v21.enums.EuPoliticalAdvertisingStatusEnum.EuPoliticalAdvertisingStatus;
import com.google.ads.googleads.v21.enums.KeywordMatchTypeEnum.KeywordMatchType;
import com.google.ads.googleads.v21.enums.ProximityRadiusUnitsEnum.ProximityRadiusUnits;
import com.google.ads.googleads.v21.enums.ServedAssetFieldTypeEnum.ServedAssetFieldType;
import com.google.ads.googleads.v21.resources.Ad;
import com.google.ads.googleads.v21.resources.AdGroup;
import com.google.ads.googleads.v21.resources.AdGroupAd;
import com.google.ads.googleads.v21.resources.AdGroupCriterion;
import com.google.ads.googleads.v21.resources.Asset;
import com.google.ads.googleads.v21.resources.AssetGroup;
import com.google.ads.googleads.v21.resources.AssetGroupAsset;
import com.google.ads.googleads.v21.resources.AssetGroupSignal;
import com.google.ads.googleads.v21.resources.BiddingStrategy;
import com.google.ads.googleads.v21.resources.Campaign;
import com.google.ads.googleads.v21.resources.CampaignCriterion;
import com.google.ads.googleads.v21.resources.Campaign.Builder;
import com.google.ads.googleads.v21.resources.Campaign.NetworkSettings;
import com.google.ads.googleads.v21.services.AdGroupAdOperation;
import com.google.ads.googleads.v21.services.AdGroupAdServiceClient;
import com.google.ads.googleads.v21.services.AdGroupCriterionOperation;
import com.google.ads.googleads.v21.services.AdGroupCriterionServiceClient;
import com.google.ads.googleads.v21.services.AdGroupOperation;
import com.google.ads.googleads.v21.services.AdGroupServiceClient;
import com.google.ads.googleads.v21.services.AssetGroupAssetOperation;
import com.google.ads.googleads.v21.services.AssetGroupOperation;
import com.google.ads.googleads.v21.services.AssetGroupSignalOperation;
import com.google.ads.googleads.v21.services.AssetOperation;
import com.google.ads.googleads.v21.services.BiddingStrategyOperation;
import com.google.ads.googleads.v21.services.BiddingStrategyServiceClient;
import com.google.ads.googleads.v21.services.CampaignCriterionOperation;
import com.google.ads.googleads.v21.services.CampaignOperation;
import com.google.ads.googleads.v21.services.CampaignServiceClient;
import com.google.ads.googleads.v21.services.GoogleAdsServiceClient;
import com.google.ads.googleads.v21.services.MutateAdGroupAdsResponse;
import com.google.ads.googleads.v21.services.MutateAdGroupCriteriaResponse;
import com.google.ads.googleads.v21.services.MutateAdGroupsResponse;
import com.google.ads.googleads.v21.services.MutateBiddingStrategiesResponse;
import com.google.ads.googleads.v21.services.MutateBiddingStrategyResult;
import com.google.ads.googleads.v21.services.MutateCampaignsResponse;
import com.google.ads.googleads.v21.services.MutateGoogleAdsResponse;
import com.google.ads.googleads.v21.services.MutateOperation;
import com.google.ads.googleads.v21.services.MutateOperationResponse;
import com.google.ads.googleads.v21.utils.ResourceNames;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;

@Component
public class SearchCampaignApi {
	
	private static final int ASSET_GROUP_TEMPORARY_ID = -3;
	private static long temporaryId = ASSET_GROUP_TEMPORARY_ID - 1;
	
	public static String createSearchCampaign(GoogleAdsClient googleAdsClient, String customerId, String budgetResourceName,
			Date startDate, Date endDate, boolean isTargetGoogleSearch, boolean isTargetSearchNetwork, boolean isTargetContentNetwork, boolean isTargetPartnerSearchNetwork, String campaignName, boolean isPortfolioBiddingStrategy, String biddingStrategy, String biddingValue) {

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");  
		boolean targetGoogleSearch = isTargetGoogleSearch;
		boolean targetSearchNetwork = isTargetSearchNetwork;
		boolean targetContentNetwork = isTargetContentNetwork;
		boolean targetPartnerSearchNetwork = isTargetPartnerSearchNetwork;

		NetworkSettings networkSettings =
				NetworkSettings.newBuilder()
				.setTargetGoogleSearch(targetGoogleSearch)
				.setTargetSearchNetwork(targetSearchNetwork)
				.setTargetContentNetwork(targetContentNetwork)
				.setTargetPartnerSearchNetwork(targetPartnerSearchNetwork)
				.build();

		BiddingStrategy portfolioBiddingStrategy =  null;
		String portfolioResourceName = "-1";

		Builder campaignBuilder =
				Campaign.newBuilder()
				.setName(campaignName)
				.setContainsEuPoliticalAdvertisingValue(EuPoliticalAdvertisingStatus.DOES_NOT_CONTAIN_EU_POLITICAL_ADVERTISING_VALUE)
				.setAdvertisingChannelType(AdvertisingChannelType.SEARCH)
				.setStatus(CampaignStatus.PAUSED)
				.setCampaignBudget(budgetResourceName)
				.setNetworkSettings(networkSettings)
				.setStartDate(dateFormat.format(startDate))
				.setEndDate(dateFormat.format(endDate));

		if(isPortfolioBiddingStrategy) {

			if(biddingStrategy.equalsIgnoreCase(SEMBiddingStrategy.BIDDING_STRATEGY_PORTFOLIO)) {

				TargetSpend targetSpend = TargetSpend.newBuilder().setCpcBidCeilingMicros(Long.valueOf(biddingValue)).build();

				portfolioBiddingStrategy =
						BiddingStrategy.newBuilder()
						.setName(biddingStrategy)
						.setTargetSpend(targetSpend)
						.build();

			}else if(biddingStrategy.equalsIgnoreCase(SEMBiddingStrategy.BIDDING_STRATEGY_PORTFOLIO_MAXIMIZE_CONVERSIONS)) {

				portfolioBiddingStrategy =
						BiddingStrategy.newBuilder()
						.setName(biddingStrategy)
						.setMaximizeConversions(MaximizeConversions.newBuilder().build())
						.build();

			}else if(biddingStrategy.equalsIgnoreCase(SEMBiddingStrategy.BIDDING_STRATEGY_PORTFOLIO_TARGET_CPA)) {

				double targetCpaValue = Double.parseDouble(SEMBiddingStrategy.getAmountMicros(biddingValue));

				portfolioBiddingStrategy =
						BiddingStrategy.newBuilder()
						.setName(biddingStrategy)
						.setTargetCpa(TargetCpa.newBuilder().setTargetCpaMicros(Double.valueOf(targetCpaValue).longValue()).build())
						.build();

			}else if(biddingStrategy.equalsIgnoreCase(SEMBiddingStrategy.BIDDING_STRATEGY_PORTFOLIO_TARGET_ROAS)) {

				double targetRoasValue = Double.parseDouble(SEMBiddingStrategy.getAmountMicros(biddingValue));

				portfolioBiddingStrategy =
						BiddingStrategy.newBuilder()
						.setName(biddingStrategy)
						.setTargetRoas(TargetRoas.newBuilder().setTargetRoas(Double.valueOf(targetRoasValue)).build())
						.build();
			}

			try (BiddingStrategyServiceClient biddingStrategyServiceClient =
					googleAdsClient.getLatestVersion().createBiddingStrategyServiceClient()) {
				BiddingStrategyOperation operation =
						BiddingStrategyOperation.newBuilder().setCreate(portfolioBiddingStrategy).build();
				MutateBiddingStrategiesResponse response =
						biddingStrategyServiceClient.mutateBiddingStrategies(
								customerId, ImmutableList.of(operation));

				MutateBiddingStrategyResult mutateBiddingStrategyResult = response.getResults(0);
				System.out.printf(
						"Created portfolio bidding strategy with resource name: '%s'.%n",
						mutateBiddingStrategyResult.getResourceName());

				portfolioResourceName =  mutateBiddingStrategyResult.getResourceName();
			}

			campaignBuilder = campaignBuilder.setBiddingStrategy(portfolioResourceName);
		}
		else {

			// if not portfolio strategy set standard bidding strategy

		//	setBiddingStrategy(biddingStrategy, biddingValue, campaignBuilder);
			System.out.println("Added Bidding Strategy ");
		}
		Campaign campaign =  campaignBuilder.build();

		CampaignOperation op = CampaignOperation.newBuilder().setCreate(campaign).build();
		String campaignResourceName ;
		try (CampaignServiceClient campaignServiceClient =
				googleAdsClient.getLatestVersion().createCampaignServiceClient()) {
			MutateCampaignsResponse response =
					campaignServiceClient.mutateCampaigns(customerId,ImmutableList.of(op));

			campaignResourceName = response.getResults(0).getResourceName();
			System.out.println("Added campaign "+ campaignResourceName);
		}
		return campaignResourceName;
	}
	
	public static String mutateAdgroup(GoogleAdsClient googleAdsClient, String customerId, String adgroupName,
			String campaignResourceName, long cpcBid, String campaignType) {

		AdGroup.Builder adGroupBuilder = AdGroup.newBuilder()
				.setName(adgroupName)
				.setStatus(AdGroupStatus.ENABLED)
				.setCampaign(campaignResourceName)
				.setCpcBidMicros(cpcBid);


		if(!campaignType.equalsIgnoreCase(GoogleCampaign.DEMAND_GEN_MULTI_ASSET_CAMPAIGN)) {

			adGroupBuilder.setType(AdGroupType.SEARCH_STANDARD)
			.build();
		}

		AdGroup adGroup = adGroupBuilder.build();

		AdGroupOperation op = AdGroupOperation.newBuilder().setCreate(adGroup).build();

		String adgroupResourceName;
		try (AdGroupServiceClient adGroupServiceClient =
				googleAdsClient.getLatestVersion().createAdGroupServiceClient()) {
			MutateAdGroupsResponse response =
					adGroupServiceClient.mutateAdGroups(customerId, ImmutableList.of(op));

			adgroupResourceName = response.getResults(0).getResourceName();
			System.out.println("Created adgroup : " + adgroupResourceName);
		}
		return adgroupResourceName;
	}
	
	public static String mutateKeyword(GoogleAdsClient googleAdsClient, String customerId, String  keyword, KeywordMatchType matType, String adGroupResourceName) {

		KeywordInfo keywordInfo =
				KeywordInfo.newBuilder().setText(keyword).setMatchType(matType).build();


		AdGroupCriterion criterion =
				AdGroupCriterion.newBuilder()
				.setAdGroup(adGroupResourceName)
				.setStatus(AdGroupCriterionStatus.ENABLED)
				.setKeyword(keywordInfo)
				.build();

		AdGroupCriterionOperation op =
				AdGroupCriterionOperation.newBuilder().setCreate(criterion).build();

		String keywordResourceName;
		try (AdGroupCriterionServiceClient agcServiceClient =
				googleAdsClient.getLatestVersion().createAdGroupCriterionServiceClient()) {
			MutateAdGroupCriteriaResponse response =
					agcServiceClient.mutateAdGroupCriteria(customerId, ImmutableList.of(op));

			keywordResourceName =  response.getResults(0).getResourceName();
			System.out.println("Created Keyword : " + keywordResourceName);

		}
		return keywordResourceName;
	}
	
	public static String mutateResponsiveAd(GoogleAdsClient googleAdsClient, String customerId, long adgroupId, List<String> headlinesList , List<String> descriptionsList, String displayUrls,
			String finalUrl, String name, String adGroupResourceNames) {

		AdTextAsset pinHeadline = createAdTextAsset(headlinesList.get(0));
		AdTextAsset pinnedHeadline =
				AdTextAsset.newBuilder()
				.setText(pinHeadline.getText())
				.setPinnedField(ServedAssetFieldType.HEADLINE_1)
				.build();
		headlinesList.remove(0);

		ResponsiveSearchAdInfo.Builder responsiveSearchAdInfo = ResponsiveSearchAdInfo.newBuilder();

		responsiveSearchAdInfo.addHeadlines(pinnedHeadline);
		for(String headlines : headlinesList) {
			responsiveSearchAdInfo.addHeadlines(createAdTextAsset(headlines));
		}
		for(String descriptions : descriptionsList) {
			responsiveSearchAdInfo.addDescriptions(createAdTextAsset(descriptions));
		}
		responsiveSearchAdInfo.build();

		Ad ad =
				Ad.newBuilder()
				.setName(name)
				.setResponsiveSearchAd(responsiveSearchAdInfo)
				.addFinalUrls(finalUrl)
				//.setDisplayUrl(displayUrl)
				.build();

		AdGroupAd adGroupAd =
				AdGroupAd.newBuilder()
				.setAdGroup(adGroupResourceNames)
				.setStatus(AdGroupAdStatus.PAUSED)
				.setAd(ad)
				.build();

		AdGroupAdOperation operation = AdGroupAdOperation.newBuilder().setCreate(adGroupAd).build();

		String adGroupAdResourceName = "-1";
		try (AdGroupAdServiceClient adGroupAdServiceClient =
				googleAdsClient.getLatestVersion().createAdGroupAdServiceClient()) {
			MutateAdGroupAdsResponse response =
					adGroupAdServiceClient.mutateAdGroupAds(
							customerId, ImmutableList.of(operation));

			adGroupAdResourceName = response.getResults(0).getResourceName();
			System.out.println("adGroupAdResourceName : "+adGroupAdResourceName);
		}
		return adGroupAdResourceName;
	}
	
	public static AdTextAsset createAdTextAsset(String text) {
		return AdTextAsset.newBuilder().setText(text).build();
	}
	
	public static String mutateCallAd(GoogleAdsClient googleAdsClient, String customerId, long adgroupId, String BusinessName , String headline1, String headline2,
			String description1, String description2,  String verificationUrl, String finalUrl, String path1, String path2, String adGroupResourceName, String phoneNumber) {

		CallAdInfo.Builder callAdInfoBuilder =
				CallAdInfo.newBuilder()
				.setBusinessName(BusinessName)
				.setHeadline1(headline1)
				.setHeadline2(headline2)
				.setDescription1(description1)
				.setDescription2(description2)
				.setCountryCode(GoogleCallAd.COUNTRY_CODE)
				.setPhoneNumber(phoneNumber)
				.setPhoneNumberVerificationUrl(verificationUrl)
				.setCallTracked(true)
				.setDisableCallConversion(false)
				.setPath1(path1)
				.setPath2(path2);

		Ad ad =
				Ad.newBuilder()
				//	.addFinalUrls(finalUrl)
				.setCallAd(callAdInfoBuilder.build())
				.build();

		AdGroupAd adGroupAd =
				AdGroupAd.newBuilder()
				.setAdGroup(adGroupResourceName)
				.setStatus(AdGroupAdStatus.PAUSED)
				.setAd(ad)
				.build();

		AdGroupAdOperation adGroupAdOperation =
				AdGroupAdOperation.newBuilder().setCreate(adGroupAd).build();

		String callAdResourceName = "-1";
		try (AdGroupAdServiceClient adGroupAdServiceClient =
				googleAdsClient.getLatestVersion().createAdGroupAdServiceClient()) {
			MutateAdGroupAdsResponse response =
					adGroupAdServiceClient.mutateAdGroupAds(
							customerId, ImmutableList.of(adGroupAdOperation));

			callAdResourceName = response.getResults(0).getResourceName();
		}

		return callAdResourceName;
	}
	
	public String createCaliperPerformanceMaxCampaign(GoogleAdsClient googleAdsClient, String customerId, String budgetResourceName, Date startDate, Date endDate, String campaignName)throws SQLException{
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd"); 
		Campaign performanceMaxCampaign =
				Campaign.newBuilder()
				.setName(campaignName)
				.setStatus(CampaignStatus.PAUSED)
				.setAdvertisingChannelType(AdvertisingChannelType.PERFORMANCE_MAX)
				.setMaximizeConversions(MaximizeConversions.getDefaultInstance())
				.setUrlExpansionOptOut(false)
		        .setBrandGuidelinesEnabled(false)
				.setCampaignBudget(budgetResourceName)
				.setStartDate(dateFormat.format(startDate))
				.setEndDate(dateFormat.format(endDate))
				.setContainsEuPoliticalAdvertisingValue(EuPoliticalAdvertisingStatus.DOES_NOT_CONTAIN_EU_POLITICAL_ADVERTISING_VALUE)
				.build();
		
		MutateOperation mutateOperation = MutateOperation.newBuilder()
				.setCampaignOperation(
						CampaignOperation.newBuilder().setCreate(performanceMaxCampaign).build())
				.build();
		
		String campaignResourceName;
		try (GoogleAdsServiceClient googleAdsServiceClient =
				googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()) {
			MutateGoogleAdsResponse response =
					googleAdsServiceClient.mutate((customerId), ImmutableList.of(mutateOperation));

			campaignResourceName = response.getMutateOperationResponses(0).getCampaignResult().getResourceName(); 
		}
		System.out.println("Created Caliper Campaign");
		return campaignResourceName;
	}

	
	public String createAndLinkTextAsset(GoogleAdsClient googleAdsClient,
			String customerId, String text, AssetFieldType assetFieldType) {
		String assetResourceName = ResourceNames.asset(Long.valueOf(customerId), getNextTemporaryId());
		Asset asset =
				Asset.newBuilder()
				.setResourceName(assetResourceName)
				.setTextAsset(TextAsset.newBuilder().setText(text).build())
				.build();
		AssetOperation assetOperation = AssetOperation.newBuilder().setCreate(asset).build();
		MutateOperation mutateOperation =MutateOperation.newBuilder().setAssetOperation(assetOperation).build();

		try (GoogleAdsServiceClient googleAdsServiceClient =
				googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()) {
			MutateGoogleAdsResponse response =
					googleAdsServiceClient.mutate(customerId, ImmutableList.of(mutateOperation) );
			MutateOperationResponse result = response.getMutateOperationResponses(0);
			assetResourceName = result.getAssetResult().getResourceName();
			System.out.println("created Text Asset "+ assetResourceName);
		}
		return assetResourceName;
	}
	
	public String createAndLinkImageAsset(GoogleAdsClient googleAdsClient,
			String customerId, String url, AssetFieldType assetFieldType, String assetName)throws IOException {

		String assetResourceName = ResourceNames.asset(Long.valueOf(customerId), getNextTemporaryId());
		byte[] assetBytes = ByteStreams.toByteArray(new URL(url).openStream());

		Asset asset =
				Asset.newBuilder()
				.setResourceName(assetResourceName)
				.setImageAsset(ImageAsset.newBuilder().setData(ByteString.copyFrom(assetBytes)).build())
				.setName(assetName)
				.build();
		AssetOperation assetOperation = AssetOperation.newBuilder().setCreate(asset).build();
		MutateOperation mutateOperation = MutateOperation.newBuilder().setAssetOperation(assetOperation).build();

		try (GoogleAdsServiceClient googleAdsServiceClient =
				googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()) {
			MutateGoogleAdsResponse response =
					googleAdsServiceClient.mutate(customerId, ImmutableList.of(mutateOperation));
			MutateOperationResponse result = response.getMutateOperationResponses(0);
			assetResourceName = result.getAssetResult().getResourceName();
			System.out.println("created Image Asset "+ assetResourceName);
		}
		return assetResourceName;
	}

	
	public static MutateOperation createAssetGroupMutateOperation(GoogleAdsClient googleAdsClient, String campaignResourceName, String assetGroupResourceName, String assetGroupName,  String finalUrl, String mobileUrl) {

		AssetGroup assetGroup =
				AssetGroup.newBuilder()
				.setName(assetGroupName)
				.setCampaign(campaignResourceName)
				.addFinalUrls(finalUrl)
				.addFinalMobileUrls(mobileUrl)
				.setStatus(AssetGroupStatus.PAUSED)
				.setResourceName(assetGroupResourceName)
				.build();
		AssetGroupOperation assetGroupOperation =
				AssetGroupOperation.newBuilder().setCreate(assetGroup).build();

		return MutateOperation.newBuilder().setAssetGroupOperation(assetGroupOperation).build();
	}

	
	public MutateOperation createAssetGroupAssetsMutateOperation(AssetFieldType fieldType, String assetGroupResourceName,
			String assetResourceName) {
		AssetGroupAsset assetGroupAsset =
				AssetGroupAsset.newBuilder()
				.setFieldType(fieldType)
				.setAssetGroup(assetGroupResourceName)
				.setAsset(assetResourceName)
				.build();

		AssetGroupAssetOperation assetGroupAssetOperation = AssetGroupAssetOperation.newBuilder()
				.setCreate(assetGroupAsset).build();

		return MutateOperation.newBuilder().setAssetGroupAssetOperation(assetGroupAssetOperation).build();
	}
	
	public MutateOperation createAssetGroupSignalOperations(
		      String customerId, String assetGroupResourceName, Long audienceId) {
		    AssetGroupSignal assetGroupSignal =
		        AssetGroupSignal.newBuilder()
		            .setAssetGroup(assetGroupResourceName)
		            .setAudience(
		                AudienceInfo.newBuilder()
		                    .setAudience(ResourceNames.audience(Long.valueOf(customerId), audienceId)))
		            .build();
		    
		    AssetGroupSignalOperation assetGroupSignalOperation = AssetGroupSignalOperation.newBuilder().setCreate(assetGroupSignal).build();
		    return MutateOperation.newBuilder().setAssetGroupSignalOperation(assetGroupSignalOperation).build();
		  }

	
	public String createAndLinkYoutubeVideoAsset(GoogleAdsClient googleAdsClient, String customerId, String youtubeVideoId,
			AssetFieldType assetFieldType, String assetName) throws MalformedURLException, IOException {
		
		String assetResourceName = ResourceNames.asset(Long.valueOf(customerId), getNextTemporaryId());

		Asset asset =
				Asset.newBuilder()
				.setResourceName(assetResourceName)
				.setYoutubeVideoAsset(YoutubeVideoAsset.newBuilder().setYoutubeVideoId(youtubeVideoId).build())
				.setName(assetName)
				.build();
		AssetOperation assetOperation = AssetOperation.newBuilder().setCreate(asset).build();
		MutateOperation mutateOperation = MutateOperation.newBuilder().setAssetOperation(assetOperation).build();

		try (GoogleAdsServiceClient googleAdsServiceClient =
				googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()) {
			MutateGoogleAdsResponse response =
					googleAdsServiceClient.mutate(customerId, ImmutableList.of(mutateOperation));
			MutateOperationResponse result = response.getMutateOperationResponses(0);
			assetResourceName = result.getAssetResult().getResourceName();
			System.out.println("created Youtube Video Asset "+ assetResourceName);
		}
		return assetResourceName;
	
	}
	private static long getNextTemporaryId() {
		return temporaryId--;
	}
	
	public String pauseCampaign(GoogleAdsClient googleAdsClient, String customerId, String campaignResourceName,long campaignId) {
		long customerIds = Long.parseLong(customerId);
		String updatedCampaignResourceName;
		
		try (CampaignServiceClient campaignServiceClient =
				googleAdsClient.getLatestVersion().createCampaignServiceClient()) {
			Campaign campaign =
					Campaign.newBuilder()
					.setResourceName(ResourceNames.campaign(customerIds, campaignId))
					.setStatus(CampaignStatus.PAUSED)
					.build();

			CampaignOperation operation =
					CampaignOperation.newBuilder()
					.setUpdate(campaign)
					.setUpdateMask(FieldMasks.allSetFieldsOf(campaign))
					.build();
			
			MutateCampaignsResponse response =
					campaignServiceClient.mutateCampaigns(
							customerId.toString(), Collections.singletonList(operation));
			
			updatedCampaignResourceName = response.getResults(0).getResourceName();
			System.out.printf("campaign paused : %s%n", updatedCampaignResourceName);
		}
		return updatedCampaignResourceName;
	}
	
	public static String associateLocationInfo(GoogleAdsClient googleAdsClient, String campaignResourceName, String customerId, double radius, String radiusUnits, int latitude,
			int longitude) {

		ProximityRadiusUnits proximityRadiusUnits = ProximityRadiusUnits.valueOf(radiusUnits);

		CampaignCriterion campaignCriterion = CampaignCriterion.newBuilder().setCampaign(campaignResourceName)
				.setProximity(ProximityInfo.newBuilder()
						.setGeoPoint(GeoPointInfo.newBuilder().setLatitudeInMicroDegrees(latitude)
								.setLongitudeInMicroDegrees(longitude))
						.setRadius(radius)
						.setRadiusUnits(proximityRadiusUnits).build())
				.build();
		
		System.out.println("Proximity criterion request: " + campaignCriterion);

		
		MutateOperation campaignCriterionOperation = MutateOperation.newBuilder().setCampaignCriterionOperation(
				CampaignCriterionOperation.newBuilder().setCreate(campaignCriterion).build()).build();
		
		    System.out.println("Operation: " + campaignCriterionOperation);
		


		String criterionResourceName = "";
		try (GoogleAdsServiceClient googleAdsServiceClient = googleAdsClient.getLatestVersion()
				.createGoogleAdsServiceClient()) {
			MutateGoogleAdsResponse response = googleAdsServiceClient.mutate(customerId, List.of(campaignCriterionOperation));
			criterionResourceName = response.getMutateOperationResponses(0).getCampaignCriterionResult().getResourceName();
		}

		System.out.println("Location Criterion created with resource name : "+criterionResourceName);
		return criterionResourceName;
	}
}
