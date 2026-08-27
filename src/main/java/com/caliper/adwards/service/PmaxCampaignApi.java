package com.caliper.adwards.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.campaign.google.entity.GoogleCampaignAssetGroup;
import com.caliper.campaign.google.entity.GoogleCampaignAssets;
import com.caliper.campaign.google.repository.GoogleCampaignAssetGroupRepository;
import com.caliper.campaign.google.repository.GoogleCampaignAssetsRepository;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v21.common.ImageAsset;
import com.google.ads.googleads.v21.common.MaximizeConversions;
import com.google.ads.googleads.v21.common.TextAsset;
import com.google.ads.googleads.v21.common.YoutubeVideoAsset;
import com.google.ads.googleads.v21.enums.AdvertisingChannelTypeEnum.AdvertisingChannelType;
import com.google.ads.googleads.v21.enums.AssetFieldTypeEnum.AssetFieldType;
import com.google.ads.googleads.v21.enums.AssetGroupStatusEnum.AssetGroupStatus;
import com.google.ads.googleads.v21.enums.BudgetDeliveryMethodEnum.BudgetDeliveryMethod;
import com.google.ads.googleads.v21.enums.CampaignStatusEnum.CampaignStatus;
import com.google.ads.googleads.v21.enums.EuPoliticalAdvertisingStatusEnum.EuPoliticalAdvertisingStatus;
import com.google.ads.googleads.v21.resources.Asset;
import com.google.ads.googleads.v21.resources.AssetGroup;
import com.google.ads.googleads.v21.resources.AssetGroupAsset;
import com.google.ads.googleads.v21.resources.Campaign;
import com.google.ads.googleads.v21.resources.CampaignBudget;
import com.google.ads.googleads.v21.services.AssetGroupAssetOperation;
import com.google.ads.googleads.v21.services.AssetGroupOperation;
import com.google.ads.googleads.v21.services.AssetOperation;
import com.google.ads.googleads.v21.services.CampaignBudgetOperation;
import com.google.ads.googleads.v21.services.CampaignBudgetServiceClient;
import com.google.ads.googleads.v21.services.CampaignOperation;
import com.google.ads.googleads.v21.services.GoogleAdsServiceClient;
import com.google.ads.googleads.v21.services.MutateCampaignBudgetsResponse;
import com.google.ads.googleads.v21.services.MutateGoogleAdsResponse;
import com.google.ads.googleads.v21.services.MutateOperation;
import com.google.ads.googleads.v21.services.MutateOperationResponse;
import com.google.ads.googleads.v21.utils.ResourceNames;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;

@Component
public class PmaxCampaignApi {
	
	@Autowired
	public static GoogleCampaignAssetsRepository googleCampaignAssetsRepository;
	
	@Autowired
	public static GoogleCampaignAssetGroupRepository googleCampaignAssetGroupRepository;

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	String timestamp = LocalDateTime.now().format(formatter);

	private static final int ASSET_GROUP_TEMPORARY_ID = -3;
	private static long temporaryId = ASSET_GROUP_TEMPORARY_ID - 1;
	
	public static String createCampaignBudget(GoogleAdsClient googleAdsClient, String customerId, long budgetAmount,
			String campaignName, boolean isSharedBudget) {
		CampaignBudget budget =
				CampaignBudget.newBuilder()
				.setName("Budget For Campaign #" +campaignName)
				.setDeliveryMethod(BudgetDeliveryMethod.STANDARD)
				.setExplicitlyShared(isSharedBudget)
				.setAmountMicros(budgetAmount)
				.build();

		CampaignBudgetOperation op = CampaignBudgetOperation.newBuilder().setCreate(budget).build();

		String budgetResourceName;
		try (CampaignBudgetServiceClient campaignBudgetServiceClient =
				googleAdsClient.getLatestVersion().createCampaignBudgetServiceClient()) {
			MutateCampaignBudgetsResponse response =
					campaignBudgetServiceClient.mutateCampaignBudgets(
							(customerId), ImmutableList.of(op));
			budgetResourceName = response.getResults(0).getResourceName();
			System.out.printf("Added budget: %s%n", budgetResourceName);
		}
		return budgetResourceName;
	}
	
	public static String createPerformanceMaxCampaign(GoogleAdsClient googleAdsClient, String customerId, String budgetResourceName, Date startDate, Date endDate, String campaignName)throws SQLException {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");    

		Campaign performanceMaxCampaign =
				Campaign.newBuilder()
				.setName(campaignName)
				.setStatus(CampaignStatus.PAUSED)
				.setAdvertisingChannelType(AdvertisingChannelType.PERFORMANCE_MAX)
				.setMaximizeConversions(MaximizeConversions.getDefaultInstance())
				.setUrlExpansionOptOut(false)
		        .setBrandGuidelinesEnabled(false)
		        .setContainsEuPoliticalAdvertising(EuPoliticalAdvertisingStatus.DOES_NOT_CONTAIN_EU_POLITICAL_ADVERTISING)
				.setCampaignBudget(budgetResourceName)
				.setStartDate(dateFormat.format(startDate))
				.setEndDate(dateFormat.format(endDate))
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
		System.out.println("Created Campaign");
		return campaignResourceName;
	}

	
	/*
	public String mutateGoogleAdSchedule(GoogleAdsClient googleAdsClient, String customerId,
			String campaignResourceName, List<GoogleAdSchedule> adChedule) {

		String criterionResourceName = "";
		for(GoogleAdSchedule schedule :adChedule) {

			String dayOfWeek = schedule.getDayOfWeek();
			DayOfWeek day = null;
			day =DayOfWeek.valueOf(dayOfWeek) ;
			int startHour = schedule.getStartHour();
			int endtHour = schedule.getEndtHour();
			MinuteOfHour startMin = MinuteOfHour.valueOf(schedule.getStartminute());
			MinuteOfHour endMin = MinuteOfHour.valueOf(schedule.getEndminute());
			System.out.println("startHour : "+startHour+" startMin : "+startMin);

			CampaignCriterion campaignCriterion = 
					CampaignCriterion.newBuilder().setCampaign(campaignResourceName).setAdSchedule( AdScheduleInfo.newBuilder()
							.setDayOfWeek(day)
							.setStartHour(startHour)
							.setEndHour(endtHour)
							.setStartMinute(startMin)
							.setEndMinute(endMin)
							.build())
					.build();

			MutateOperation campaignCriterionOperation = MutateOperation.newBuilder().setCampaignCriterionOperation(
					CampaignCriterionOperation.newBuilder().setCreate(campaignCriterion).build()).build();

			try (GoogleAdsServiceClient googleAdsServiceClient = googleAdsClient.getLatestVersion()
					.createGoogleAdsServiceClient()) {
				MutateGoogleAdsResponse response = googleAdsServiceClient.mutate(customerId, List.of(campaignCriterionOperation));
				criterionResourceName = response.getMutateOperationResponses(0).getCampaignCriterionResult().getResourceName();
			}
			System.out.println("Ad Schedule Criterion created with resource name : "+criterionResourceName);
		}
		return criterionResourceName;
	}

	public String mutateCallExtension(GoogleAdsClient googleAdsClient, String customerId, String phoneNumber,
			long conversionActionId, List<GoogleAdSchedule> adChedule, String campaignResourceName) {

		String callAssetResourceName = "";


		CallAsset.Builder callAssetBuilder =
				CallAsset.newBuilder()
				.setCountryCode(GoogleCallAd.COUNTRY_CODE)
				.setPhoneNumber(phoneNumber);

		AssetOperation assetOperation =
				AssetOperation.newBuilder()
				.setCreate(Asset.newBuilder().setCallAsset(callAssetBuilder.build()).build())
				.build();

		try (AssetServiceClient assetServiceClient =
				googleAdsClient.getLatestVersion().createAssetServiceClient()) {
			MutateAssetsResponse response =
					assetServiceClient.mutateAssets(
							customerId, ImmutableList.of(assetOperation));
			callAssetResourceName = response.getResults(0).getResourceName();
			System.out.println(
					"Created a call asset with resource name: "+ callAssetResourceName);
		}

		//DELETE THE BELOW CODE IF ABOVE WORKS
		adChedule = new ArrayList<>();
		for(GoogleAdSchedule schedule :adChedule) {

			String dayOfWeek = schedule.getDayOfWeek();
			DayOfWeek day = null;

			day =DayOfWeek.valueOf(dayOfWeek) ;

			int startHour = schedule.getStartHour();
			int endtHour = schedule.getEndtHour();
			MinuteOfHour startMin = MinuteOfHour.valueOf(schedule.getStartminute());
			MinuteOfHour endMin = MinuteOfHour.valueOf(schedule.getEndminute());

			CallAsset.Builder callAssetBuilder1 =
					CallAsset.newBuilder()
					.setCountryCode(GoogleCallAd.COUNTRY_CODE)
					.setPhoneNumber(phoneNumber)
					.addAdScheduleTargets(
							AdScheduleInfo.newBuilder()
							.setDayOfWeek(day)
							.setStartHour(startHour)
							.setEndHour(endtHour)
							.setStartMinute(startMin)
							.setEndMinute(endMin)
							.build());

			AssetOperation assetOperation1 =
					AssetOperation.newBuilder()
					.setCreate(Asset.newBuilder().setCallAsset(callAssetBuilder.build()).build())
					.build();

			try (AssetServiceClient assetServiceClient =
					googleAdsClient.getLatestVersion().createAssetServiceClient()) {
				MutateAssetsResponse response =
						assetServiceClient.mutateAssets(
								customerId, ImmutableList.of(assetOperation));
				callAssetResourceName = response.getResults(0).getResourceName();
				System.out.println(
						"Created a call asset with resource name: "+ callAssetResourceName);
			}
		}
		return callAssetResourceName;
	}

	public String associateCallAssetWithCampaign(
			GoogleAdsClient googleAdsClient, String customerId, String callAssetResourceName, String campaignResourceName) {
		String campaignAssetResourceName = "";
		try (CampaignAssetServiceClient campaignAssetServiceClient =
				googleAdsClient.getLatestVersion().createCampaignAssetServiceClient()) {

			CampaignAsset campaignAsset = CampaignAsset.newBuilder()
					.setFieldType(AssetFieldType.CALL)
					.setAsset(callAssetResourceName)
					.setCampaign(campaignResourceName)
					.build();

			CampaignAssetOperation operation = CampaignAssetOperation.newBuilder()
					.setCreate(campaignAsset)
					.build();

			MutateCampaignAssetsResponse response =
					campaignAssetServiceClient.mutateCampaignAssets(customerId, ImmutableList.of(operation));
			campaignAssetResourceName = response.getResults(0).getResourceName();
			System.out.printf("CallAsset associated with campaign campaignAssetResourceName : "+ campaignAssetResourceName);

		} catch (GoogleAdsException gae) {
			System.err.printf("Google Ads API request failed. Please check your parameters and try again.%n");
			System.exit(1);
		}
		return campaignAssetResourceName;
	}

	public void setBiddingStrategy(String biddingStrategy, String biddingValue, Builder campaignBuilder) {
		SEMBiddingStrategy semBiddingStrategy= SEMBiddingStrategyFactory.getSEMBiddingStrategy(biddingStrategy, biddingValue);
		semBiddingStrategy.setBiddingStrategy(campaignBuilder);
	}
*/
	
	private static long getNextTemporaryId() {
		return temporaryId--;
	}
	
	public static String createAndLinkTextAsset(GoogleAdsClient googleAdsClient,
			String customerId, String text, AssetFieldType assetFieldType) {
		String assetResourceName = ResourceNames.asset(Long.valueOf(customerId), getNextTemporaryId());
		// Creates the Text Asset.
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
	
	public static String createAndLinkImageAsset(GoogleAdsClient googleAdsClient,
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

	public static String createAndLinkYoutubeVideoAsset(GoogleAdsClient googleAdsClient, String customerId, String youtubeVideoId,
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
	
	public static MutateOperation createAssetGroupMutateOperation(GoogleAdsClient googleAdsClient, String campaignResourceName, String assetGroupResourceName, String assetGroupName,  String finalUrl, String mobileUrl) {

		//	  String assetGroupResourceName = ResourceNames.assetGroup(customerId, ASSET_GROUP_TEMPORARY_ID);
		//	  String assetGroupResourceName =  "customers/1671381783/assetGroups/6459839200"; //ResourceNames.assetGroup(customerId, ASSET_GROUP_TEMPORARY_ID);
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
	
	public static MutateOperation createAssetGroupAssetsMutateOperation(AssetFieldType fieldType, String assetGroupResourceName,
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
	
	public static void updateResourceNamesInDb(MutateGoogleAdsResponse response, String customerId,
			Map<String, Long> assetResourceNameVsId, long assetGroupId) throws IOException, SQLException{
		String suffix = "_result";
		for(MutateOperationResponse result : response.getMutateOperationResponsesList()) {
			for (Entry<FieldDescriptor, Object> responseFields : result.getAllFields().entrySet()) {
				String fieldName = responseFields.getKey().getName();
				if (fieldName.endsWith(suffix)) {
					fieldName = fieldName.substring(0, fieldName.length() - suffix.length());
				}
				String value = responseFields.getValue().toString().trim();
				System.out.printf("Created a(n) %s with %s.%n", fieldName, value);
				if(fieldName.equalsIgnoreCase("asset_group")) {
					String assetGroupResourceName = value.substring(value.indexOf("\"")+1,value.lastIndexOf("\"") );
					System.out.println("Inserting in asset Group resource name");
					GoogleCampaignAssetGroup googleCampaignAssetGroup = googleCampaignAssetGroupRepository.findById(assetGroupId).orElseThrow(() -> new InvalidRequestException("Asset Group not found"));
					googleCampaignAssetGroup.setAssetGroupResourceName(assetGroupResourceName);
					googleCampaignAssetGroupRepository.save(googleCampaignAssetGroup);
				} else if (fieldName.equalsIgnoreCase("asset_group_asset")){
					String assetGroupAssetResourceName = value.substring(value.indexOf("\"")+1,value.lastIndexOf("\"") );
					String fieldNameArray[] = assetGroupAssetResourceName.split("~");
					String assetResourceName = ResourceNames.asset(Long.valueOf(customerId),
							Long.valueOf(fieldNameArray[1]));
					Long assetID = assetResourceNameVsId.get(assetResourceName);
					System.out.println("Inserting asset Group asset resource name");
					GoogleCampaignAssets googleCampaignAsset = googleCampaignAssetsRepository.findById(assetID).orElseThrow(() -> new InvalidRequestException("Asset not found"));
					googleCampaignAsset.setAssetGroupAssetResourceName(assetGroupAssetResourceName);
					googleCampaignAssetsRepository.save(googleCampaignAsset);
				}
			}
		}
	}
	
	public static void createAndMutateAssets(String customerId, GoogleAdsClient googleAdsClient, List<GoogleCampaignAssets>googleCampaignAssets) throws IOException {
		for(GoogleCampaignAssets campaignAsset : googleCampaignAssets) {
			Long assetId = campaignAsset.getId();
			String assetResourceName = campaignAsset.getAssetGroupAssetResourceName();
			if(assetResourceName.equalsIgnoreCase("-1")) {
				String type = campaignAsset.getType();
				
				if(type.equalsIgnoreCase(AssetFieldType.HEADLINE.name())
						|| type.equalsIgnoreCase(AssetFieldType.LONG_HEADLINE.name())
						|| type.equalsIgnoreCase(AssetFieldType.DESCRIPTION.name())
						|| type.equalsIgnoreCase(AssetFieldType.BUSINESS_NAME.name())) {
					String text = campaignAsset.getValue();
					assetResourceName = createAndLinkTextAsset(googleAdsClient, customerId, text, AssetFieldType.valueOf(type));
				}else if(type.equalsIgnoreCase(AssetFieldType.LOGO.name())
						|| type.equalsIgnoreCase(AssetFieldType.MARKETING_IMAGE.name())
						|| type.equalsIgnoreCase(AssetFieldType.SQUARE_MARKETING_IMAGE.name())
						|| type.equalsIgnoreCase(AssetFieldType.PORTRAIT_MARKETING_IMAGE.name())
						|| type.equalsIgnoreCase(AssetFieldType.LANDSCAPE_LOGO.name())){
					String url = campaignAsset.getValue();
					String assetName = type+"-"+campaignAsset.getId();
					assetResourceName = createAndLinkImageAsset(googleAdsClient, customerId, url,
							AssetFieldType.valueOf(type), assetName);
				}else if(type.equalsIgnoreCase(AssetFieldType.YOUTUBE_VIDEO.name())) {
					String youtubeVideoId = campaignAsset.getValue();
					String assetName = type+"-"+campaignAsset.getId();
					assetResourceName = createAndLinkYoutubeVideoAsset(googleAdsClient, customerId, youtubeVideoId,
							AssetFieldType.valueOf(type), assetName);
				}
				
				campaignAsset.setAssetResourceName(assetResourceName);
				googleCampaignAssetsRepository.save(campaignAsset);
			}
		}
	}
}