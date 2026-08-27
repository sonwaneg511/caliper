package com.caliper.task;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.caliper.adwards.service.PmaxCampaignApi;
import com.caliper.adwards.service.SearchCampaignApi;
import com.caliper.campaign.google.entity.GoogleAdgroup;
import com.caliper.campaign.google.entity.GoogleCallAd;
import com.caliper.campaign.google.entity.GoogleCampaign;
import com.caliper.campaign.google.entity.GoogleCampaignAssetGroup;
import com.caliper.campaign.google.entity.GoogleCampaignAssets;
import com.caliper.campaign.google.entity.GoogleCampaignGeoDetails;
import com.caliper.campaign.google.entity.GoogleKeyword;
import com.caliper.campaign.google.entity.GoogleResponsiveAd;
import com.caliper.campaign.google.repository.GoogleAdgroupRepository;
import com.caliper.campaign.google.repository.GoogleCallAdRepository;
import com.caliper.campaign.google.repository.GoogleCampaignAssetGroupRepository;
import com.caliper.campaign.google.repository.GoogleCampaignAssetsRepository;
import com.caliper.campaign.google.repository.GoogleCampaignGeoDetailsRepository;
import com.caliper.campaign.google.repository.GoogleCampaignRepository;
import com.caliper.campaign.google.repository.GoogleKeywordRepository;
import com.caliper.campaign.google.repository.GoogleResponsiveAdRepository;
import com.caliper.campaign.google.service.CampaignService;
import com.caliper.job.runtime.ExecutableJob;
import com.caliper.keywordPlanner.entity.GoogleAccount;
import com.caliper.keywordPlanner.repository.GoogleAccountRepository;
import com.caliper.utils.campaign.GoogleSessionFactory;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v21.enums.AssetFieldTypeEnum.AssetFieldType;
import com.google.ads.googleads.v21.enums.KeywordMatchTypeEnum.KeywordMatchType;
import com.google.ads.googleads.v21.services.GoogleAdsServiceClient;
import com.google.ads.googleads.v21.services.MutateGoogleAdsResponse;
import com.google.ads.googleads.v21.services.MutateOperation;
import com.google.ads.googleads.v21.utils.ResourceNames;

public class GoogleCampaignDeploymentTaskTest implements ExecutableJob{

	@Autowired
	private CampaignService campaignService;
	
	@Autowired
	private GoogleAccountRepository googleAccountRepository;
	
	@Autowired
	private GoogleCampaignRepository googleCampaignRepository;
	
	@Autowired
	private GoogleCampaignGeoDetailsRepository googleCampaignGeoDetailsRepository;
	
	@Autowired
	private GoogleAdgroupRepository googleAdgroupRepository;
	
	@Autowired
	private GoogleKeywordRepository googleKeywordRepository;
	
	@Autowired
	private GoogleResponsiveAdRepository googleResponsiveAdRepository;
	
	@Autowired
	private GoogleCallAdRepository googleCallAdRepository;
	
	@Autowired
	private GoogleCampaignAssetGroupRepository googleCampaignAssetGroupRepository;
	
	@Autowired
	private GoogleCampaignAssetsRepository googleCampaignAssetsRepository;
	
	private static final int ASSET_GROUP_TEMPORARY_ID = -3;
	
	@Override
	public void run(Map<String, String> params) throws Exception {
		deployGoogleCaliperCampaign(params);
	}

	private void deployGoogleCaliperCampaign(Map<String, String> params) throws IOException, SQLException {
		String clientId = params.get("client-id");
		
		List<GoogleCampaign> googleCampaigns = campaignService.findAllGoogleCampaignByStatus(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PENDING_DEPLOYMENT);
		
		for(GoogleCampaign campaign : googleCampaigns) {
			String customerId = campaign.getGoogleAccountID();
			GoogleAccount googleAccount = googleAccountRepository.findByAccountId(customerId);
			long loginCustomerId = googleAccount.getLoginCustomerId();
			GoogleAdsClient googleAdsClient = GoogleSessionFactory.getGoogleAdsClientByLoginCustomerID(loginCustomerId);
			
			log("Processing Campaign : "+campaign.getCampaignName() +" & Account : "+googleAccount.getAccountId());
			
			// CREATE BUDGET
			
			String budgetResourceName = campaign.getBudgetResourceName();
			
			if(budgetResourceName.equalsIgnoreCase("-1")) {
				log("Creating Budget ");
				long budgetAmount = (campaign.getDailyBudget().longValue())*1000000;
				budgetResourceName = PmaxCampaignApi.createCampaignBudget(googleAdsClient, customerId, budgetAmount, campaign.getCampaignName(), false);
				
				log("Created budget with resource name : "+budgetResourceName);
				
				campaign.setBudgetResourceName(budgetResourceName);
				googleCampaignRepository.save(campaign);
			}
			
			//CREATE CAMPAIGN
			
			String campaignResourceName = campaign.getCampaignResourceName();
			
			if(campaignResourceName.equalsIgnoreCase("-1")) {
				Date startDate = campaign.getStartDate();
				Date endDate = campaign.getEndDate();
				
				log("Creating Campaign");
				if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.SEARCH_CAMPAIGN) || campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.CALL_AD_CAMPAIGN)) {
					campaignResourceName = SearchCampaignApi.createSearchCampaign(googleAdsClient, customerId, budgetResourceName, startDate, endDate, campaign.isTargetGoogleSearch(), campaign.isTargetSearchNetwork(), campaign.isTargetContentNetwork(), campaign.isTargetPartnerSearchNetwork(), campaign.getCampaignName(), campaign.isPortfolioBiddingStrategy(), campaign.getBiddingStrategy(), campaign.getBiddingValue());
				}
				if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.PMAX_CAMPAIGN)) {
					campaignResourceName = PmaxCampaignApi.createPerformanceMaxCampaign(googleAdsClient, customerId, budgetResourceName, startDate, endDate, campaign.getCampaignName());
				}
				log("Created campaign with resource name : "+campaignResourceName);
				campaign.setCampaignResourceName(campaignResourceName);
				googleCampaignRepository.save(campaign);
			}
			
			//LATITUDE AND LONGITUDE
			
			String locationInfoResourceName = campaign.getLocationInfoResourceName();
			if(locationInfoResourceName.equalsIgnoreCase("-1") && campaign.isRadiusTarget()) {
				List<GoogleCampaignGeoDetails> geoDetailsList = googleCampaignGeoDetailsRepository.findAllGoogleCampaignGeoDetailsByCampaignId(campaign.getId()); 
				
				double radius = 0.0;
				String latitude = "";
				String longitude = "";
				String radiusUnits ="";
				long geoTargetingId = 0L;
				
				for(GoogleCampaignGeoDetails geoDetail : geoDetailsList) {
					if(geoDetail.getTargetingType().equalsIgnoreCase(GoogleCampaignGeoDetails.RADIUS)) {
						radius = Double.valueOf(geoDetail.getTargetingDisplayName());
					}
					if(geoDetail.getTargetingType().equalsIgnoreCase(GoogleCampaignGeoDetails.RADIUS_UNIT)) {
						radiusUnits = geoDetail.getTargetingDisplayName();
					}
					if(geoDetail.getTargetingType().equalsIgnoreCase(GoogleCampaignGeoDetails.LATITUDE)) {
						latitude = geoDetail.getTargetingDisplayName();
					}
					if(geoDetail.getTargetingType().equalsIgnoreCase(GoogleCampaignGeoDetails.LONGITUDE)) {
						longitude = geoDetail.getTargetingDisplayName();
					}
					if(geoDetail.getTargetingType().equalsIgnoreCase(GoogleCampaignGeoDetails.CITY)) {
						geoTargetingId = geoDetail.getGeoTargetingId();
					}
				}
				int lat = (int) ((Double.valueOf(latitude))*1000000);
				int longi = (int) ((Double.valueOf(longitude))*1000000);
				
				log("Creating Location Info");
				locationInfoResourceName = SearchCampaignApi.associateLocationInfo(googleAdsClient, campaignResourceName, customerId, radius, radiusUnits, lat, longi);
			}
			
			//CREATE ADGROUP
			if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.SEARCH_CAMPAIGN) || campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.PMAX_CAMPAIGN) || campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.CALL_AD_CAMPAIGN)) {
				List<GoogleAdgroup> googleAdGroups = googleAdgroupRepository.findByCampaignId(campaign.getId());
				
				if(googleAdGroups.size()>0) {
					for(GoogleAdgroup adgroup : googleAdGroups) {
						long adgroupId = adgroup.getId();
						long cpcBid = (Long.parseLong(adgroup.getCpcBid()))*1000000;
						String adgroupResourceName = adgroup.getAdGroupResourceName();
						
						if(adgroupResourceName.equalsIgnoreCase("-1")) {
							log("Creating  Adgroup");
							adgroupResourceName = SearchCampaignApi.mutateAdgroup(googleAdsClient, customerId, adgroup.getAdGroupName(), campaignResourceName, cpcBid, campaign.getCampaignStructureType());
							log("Created Adgroup with resource name : "+adgroupResourceName);
							adgroup.setAdGroupResourceName(adgroupResourceName);
							googleAdgroupRepository.save(adgroup);
						}
						
						if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.SEARCH_CAMPAIGN) || campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.CALL_AD_CAMPAIGN)) {
							//ADD KEYWORDS
							List<GoogleKeyword> googleKeywords = googleKeywordRepository.findByAdgroupId(adgroupId);
							if(googleKeywords.size()>0) {
								for(GoogleKeyword keyword : googleKeywords) {
									long keywordId = keyword.getId();
									String keywordResourceName = keyword.getKeywordResourceName();
									
									if(keywordResourceName.equalsIgnoreCase("-1")) {
										String kwdArr[] = keyword.getKeyword().split(" ");
										if(kwdArr.length>10) {
											continue;
										}else {
											String kwd = keyword.getKeyword();
											KeywordMatchType matchType =  KeywordMatchType.valueOf(keyword.getMatchType());
											log("Creating  keyword");
											keywordResourceName = SearchCampaignApi.mutateKeyword(googleAdsClient, customerId, kwd, matchType, adgroupResourceName);
											log("Created keyword with resource name : "+keywordResourceName);
											GoogleKeyword googleKeyword = googleKeywordRepository.findById(keywordId).orElseThrow(() -> new InvalidRequestException("keyword not found"));
											googleKeyword.setKeywordResourceName(keywordResourceName);
											googleKeywordRepository.save(googleKeyword);
										}
									}
								}
							}else {
								log("Keyword list is empty for adgroup - "+adgroupId);
							} 
						}
						//RESPONSIVE ADD
						if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.SEARCH_CAMPAIGN)) {
							List<GoogleResponsiveAd> googleResponsiveAds = googleResponsiveAdRepository.findAllByAdgroupId(adgroupId);
							
							if(googleResponsiveAds.size()>0) {
								String responsiveAdResourceName = googleResponsiveAds.get(0).getAdResourceName();
								List<String> headlineList = new ArrayList<>();
								List<String> descriptionList = new ArrayList<>();
								String displayUrl = "";
								String finalUrl = "";
								String adName = "";
								
								if(responsiveAdResourceName.equalsIgnoreCase("-1")) {
									for(GoogleResponsiveAd responsiveAd : googleResponsiveAds) {
										String type = responsiveAd.getType();
										
										if(type.equalsIgnoreCase(GoogleResponsiveAd.HEADLINE)) {
											headlineList.add(responsiveAd.getValue());
										}
										else if(type.equalsIgnoreCase(GoogleResponsiveAd.DESCRIPTION)) {
											descriptionList.add(responsiveAd.getValue());
										}
										else if(responsiveAd.getType().equalsIgnoreCase(GoogleResponsiveAd.AD_NAME)) {
											adName = responsiveAd.getValue();
										}
										else if(responsiveAd.getType().equalsIgnoreCase(GoogleResponsiveAd.FINAL_URL)) {
											finalUrl = responsiveAd.getValue();
										}
										else if(responsiveAd.getType().equalsIgnoreCase(GoogleResponsiveAd.DISPLAY_URL)) {
											displayUrl = responsiveAd.getValue();
										}
									}
									log("Creating responsive ad");
									responsiveAdResourceName = SearchCampaignApi.mutateResponsiveAd(googleAdsClient, customerId, adgroupId, headlineList, descriptionList, displayUrl, finalUrl, adName, adgroupResourceName);
									log("Created responsive ad with resource name : "+responsiveAdResourceName);
									GoogleResponsiveAd googleResponsiveAd = googleResponsiveAdRepository.findByAdgroupId(adgroupId);
									googleResponsiveAd.setAdResourceName(responsiveAdResourceName);
									googleResponsiveAdRepository.save(googleResponsiveAd);
								}
							} else {
								log("Responsive Ad list is empty for adgroup id - "+adgroupId);
							}
						}
						// ADD CALL AD
						if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.CALL_AD_CAMPAIGN)) {
							GoogleCallAd callAd = googleCallAdRepository.findByAdGroupId(adgroupId).orElseThrow(() -> new InvalidRequestException("call ad not found"));
							if(callAd != null) {
								long adId = callAd.getId();
								String callAdResourceName = callAd.getAdResourceName();
								
								if(callAdResourceName.equalsIgnoreCase("-1")) {
									String phoneNumber = callAd.getPhoneNumber();
									String businessName = callAd.getBusinessName();
									String headline1 = callAd.getHeadline1();
									String headline2 = callAd.getHeadline2();
									String description1 = callAd.getDescription1();
									String description2 = callAd.getDescription2();
									String verificationUrl = callAd.getVerificationUrl();
									String finalUrl = callAd.getFinalUrl();
									String path1 = callAd.getPath1();
									String path2 = callAd.getPath2();
									
									log("Creating call ad");
									callAdResourceName = SearchCampaignApi.mutateCallAd(googleAdsClient, customerId, adgroupId, businessName, headline1, headline2, description1, description2, verificationUrl, finalUrl, path1, path2, adgroupResourceName, phoneNumber);
									
									log("Created responsive ad with resource name : "+callAdResourceName);
									GoogleCallAd googleCallAd = googleCallAdRepository.findById(adId).orElseThrow(() -> new InvalidRequestException("call ad not found"));
									googleCallAd.setAdResourceName(callAdResourceName);
									googleCallAdRepository.save(googleCallAd);
								}
							} else {
								log("Call ad list is empty for adgroup id : "+adgroupId);
							}
						}
						
						
					}
				} else {
					log("Adgroup list is empty for campaign - "+campaign.getCampaignName());
				}
			}
			//================================================= PMAX CAMPAIGN =============================================================================================
			if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.PMAX_CAMPAIGN)) {
				List<GoogleCampaignAssetGroup> googleCampaignAssetGroups = googleCampaignAssetGroupRepository.findAllByCampaignId(campaign.getId());
				
				if(googleCampaignAssetGroups.size()>0) {
					for(GoogleCampaignAssetGroup assetGroup : googleCampaignAssetGroups) {
						long assetGroupId = assetGroup.getId();
						
						Map<String, Long> assetResourceNameVsId = new HashMap<>();
						List<MutateOperation> mutateOperations = new ArrayList<>();
						
						String assetGroupResourceName = assetGroup.getAssetGroupResourceName();
						
						if(assetGroupResourceName.equalsIgnoreCase("-1")) {
							String assetGroupName = assetGroup.getAssetGroupName();
							assetGroupResourceName = ResourceNames.assetGroup(Long.valueOf(customerId),
									ASSET_GROUP_TEMPORARY_ID);
							
							String finalUrl = assetGroup.getFinalUrl();
							String mobileUrl = assetGroup.getMobileUrl();
							mutateOperations.add(PmaxCampaignApi.createAssetGroupMutateOperation(googleAdsClient,
									campaignResourceName, assetGroupResourceName, assetGroupName, finalUrl, mobileUrl));
							log("Created Asset Group with resource name : "+assetGroupResourceName);
						}
						
						List<GoogleCampaignAssets> googleCampaignAssets = googleCampaignAssetsRepository.findByAssetGroupId(assetGroupId);
						
						if(googleCampaignAssets.size()>0) {
							PmaxCampaignApi.createAndMutateAssets(customerId, googleAdsClient, googleCampaignAssets);
							
							for(GoogleCampaignAssets googleCampaignAsset : googleCampaignAssets) {
								Long assetId = googleCampaignAsset.getId();
								String assetResourceName = googleCampaignAsset.getAssetResourceName();
								String assetGroupAssetResourceName = googleCampaignAsset.getAssetGroupAssetResourceName();
								
								if(!assetResourceName.equalsIgnoreCase("-1") && assetGroupAssetResourceName.equalsIgnoreCase("-1")) {
									mutateOperations.add(PmaxCampaignApi.createAssetGroupAssetsMutateOperation(
											AssetFieldType.valueOf(googleCampaignAsset.getType()), assetGroupResourceName,
											googleCampaignAsset.getAssetResourceName()));
									assetResourceNameVsId.put(assetResourceName, assetId);
									log("Created Asset with resource name : "+assetResourceName);
								}
							}
						} else {
							log("Assets list is Empty for Asset Group - "+assetGroup.getAssetGroupName());
							continue;
						}
						
						if(!mutateOperations.isEmpty()) {
							try (GoogleAdsServiceClient googleAdsServiceClient = googleAdsClient.getLatestVersion()
									.createGoogleAdsServiceClient()) {
								MutateGoogleAdsResponse response = googleAdsServiceClient.mutate(customerId, mutateOperations);
								PmaxCampaignApi.updateResourceNamesInDb(response, customerId, assetResourceNameVsId, assetGroupId);
							}
						}
					}
				} else {
					log("Asset Group list is empty for campaign - "+campaign.getCampaignName());
				}
			}
			campaign.setStatus(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_DEPLOYED);
			googleCampaignRepository.save(campaign);
		}
	}

}
