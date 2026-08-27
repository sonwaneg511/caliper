package com.caliper.campaign.google.service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.caliper.adwards.service.SearchCampaignApi;
import com.caliper.campaign.google.dto.request.CampaignFilterRequest;
import com.caliper.campaign.google.dto.request.ClientAccountSetupDto;
import com.caliper.campaign.google.dto.request.ClientLocationDetailsRequest;
import com.caliper.campaign.google.dto.request.ClientLocationSetupDto;
import com.caliper.campaign.google.dto.request.ClientOnboardingDetailsDto;
import com.caliper.campaign.google.dto.request.LocationDetailsRequest;
import com.caliper.campaign.google.dto.response.CallAdViewCampaignSetupDetailsResponse;
import com.caliper.campaign.google.dto.response.CampaignDealerLocationDetails;
import com.caliper.campaign.google.dto.response.CampaignLocationDetails;
import com.caliper.campaign.google.dto.response.ClientData;
import com.caliper.campaign.google.dto.response.ClientKeywordsDTO;
import com.caliper.campaign.google.dto.response.ClientLocationDetailsDto;
import com.caliper.campaign.google.dto.response.IndustryDetailsDto;
import com.caliper.campaign.google.dto.response.PmaxViewCampaignSetupDetailsResponse;
import com.caliper.campaign.google.dto.response.PopulateClientKeywordDetailsDto;
import com.caliper.campaign.google.dto.response.PopulateClientLocationDetailsDto;
import com.caliper.campaign.google.dto.response.PopulateClientOnboardingDetailsDto;
import com.caliper.campaign.google.dto.response.SearchViewCampaignSetupDetailsResponse;
import com.caliper.campaign.google.dto.response.SelfServeResponse;
import com.caliper.campaign.google.dto.response.ViewAllCampaignsResponse;
import com.caliper.campaign.google.dto.response.ViewCampaignSetupDetailsResponse;
import com.caliper.campaign.google.dto.response.ViewPageableCampaignResponse;
import com.caliper.campaign.google.entity.ArtAdwordsData;
import com.caliper.campaign.google.entity.BaseKeywords;
import com.caliper.campaign.google.entity.ClientAccountSetup;
import com.caliper.campaign.google.entity.ClientDataSetup;
import com.caliper.campaign.google.entity.ClientDataSetupKeywords;
import com.caliper.campaign.google.entity.ClientIndustryDetails;
import com.caliper.campaign.google.entity.ClientLocationSetup;
import com.caliper.campaign.google.entity.GoogleAdgroup;
import com.caliper.campaign.google.entity.GoogleCallAd;
import com.caliper.campaign.google.entity.GoogleCampaign;
import com.caliper.campaign.google.entity.GoogleCampaignAssetGroup;
import com.caliper.campaign.google.entity.GoogleCampaignAssets;
import com.caliper.campaign.google.entity.GoogleResponsiveAd;
import com.caliper.campaign.google.repository.ArtAdwordsDataBQRepository;
import com.caliper.campaign.google.repository.BaseKeywordsRepository;
import com.caliper.campaign.google.repository.ClientAccountSetupRepository;
import com.caliper.campaign.google.repository.ClientDataSetupKeywordsRepository;
import com.caliper.campaign.google.repository.ClientDataSetupRepository;
import com.caliper.campaign.google.repository.ClientIndustryDetailsRepository;
import com.caliper.campaign.google.repository.ClientLocationSetupRepository;
import com.caliper.campaign.google.repository.GoogleAdgroupRepository;
import com.caliper.campaign.google.repository.GoogleCallAdRepository;
import com.caliper.campaign.google.repository.GoogleCampaignAssetGroupRepository;
import com.caliper.campaign.google.repository.GoogleCampaignAssetsRepository;
import com.caliper.campaign.google.repository.GoogleCampaignRepository;
import com.caliper.campaign.google.repository.GoogleResponsiveAdRepository;
import com.caliper.keywordPlanner.entity.GoogleAccount;
import com.caliper.keywordPlanner.repository.GoogleAccountRepository;
import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.entity.Client;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.repository.ClientRepository;
import com.caliper.location.repository.DealerLocationRepository;
import com.caliper.location.service.DealerLocationService;
import com.caliper.review.dto.request.ReviewRequest;
import com.caliper.usermanagement.entity.UserClientLocMapping;
import com.caliper.usermanagement.repository.UserClientLocMappingRepository;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.caliper.utils.gemini.dto.response.GeminiResponse;
import com.caliper.utils.gemini.dto.response.GeminiResponse.Candidate;
import com.caliper.utils.gemini.dto.response.GeminiResponse.Content;
import com.caliper.utils.gemini.dto.response.GeminiResponse.Part;
import com.caliper.utils.gemini.service.GeminiAIService;
import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v21.enums.AssetFieldTypeEnum.AssetFieldType;
import com.google.cloud.bigquery.JobException;

import jakarta.transaction.Transactional;

@Service
public class CampaignService {

	private static final Logger log = LoggerFactory.getLogger(CampaignService.class);

	@Autowired
	public BaseKeywordsRepository baseKeywordsRepository;

	@Autowired
	public ClientAccountSetupRepository clientAccountSetupRepository;

	@Autowired
	public GeminiAIService geminiAIService;

	@Autowired
	public ClientLocationSetupRepository clientLocationSetupRepository;

	@Autowired
	public ClientRepository clientRepository;

	@Autowired
	public GoogleAccountRepository accountRepository;

	@Autowired
	public ClientDataSetupKeywordsRepository clientDataSetupKeywordsRepository;

	@Autowired
	public ClientDataSetupRepository clientDataSetupRepository;

	@Autowired
	public GoogleCampaignRepository googleCampaignRepository;

	@Autowired
	public GoogleCampaignAssetGroupRepository googleCampaignAssetGroupRepository;

	@Autowired
	public GoogleCampaignAssetsRepository googleCampaignAssetsRepository;

	@Autowired
	public ArtAdwordsDataBQRepository artAdwordsDataBQRepository;

	@Autowired
	public GoogleAdgroupRepository googleAdgroupRepository;

	@Autowired
	public GoogleResponsiveAdRepository googleResponsiveAdRepository;

	@Autowired
	public GoogleCallAdRepository googleCallAdRepository;

	@Autowired
	public ClientIndustryDetailsRepository clientIndustryDetailsRepository;

	@Autowired
	public DealerLocationService dealerLocationService;

	@Autowired
	public SearchCampaignApi searchCampaignApi;

	@Autowired
	public UserClientLocMappingRepository userClientLocMappingRepository;
	
	@Autowired
	private DealerLocationRepository dealerLocationRepository;
	
	//location request creation
	public LocationFilterRequest locationFilterRequestCreate(CampaignFilterRequest req) {

		LocationFilterRequest locationRequest = LocationFilterRequest.builder()
				.clientId(req.getClientId())
				.userId(req.getUserId())
				.state(req.getState())
				.city(req.getCity())
				.dealerId(req.getDealerId())
				.country(req.getCountry())
				.build();

		return locationRequest;
	}

	//onboarding campaign
	@Transactional
	public void insertClientDetails(@RequestBody ClientAccountSetupDto dto) throws InterruptedException {

		//client account setup entry
		ClientAccountSetup clientAccountSetup = ClientAccountSetup.builder()
				.clientId(dto.getClientId())
				.industry(dto.getIndustry())
				.subIndustry(dto.getSubIndustry())
				.youtubeVideoUrl(dto.getYoutubeUrl())
				.cpcBid(dto.getCpcBid())
				.platform(dto.getPlatform())
				.keywordSource(dto.getKeywordSource())
				.monthlyBudget(dto.getMonthlyBudget())
				.objective(dto.getObjective())
				.build();
		clientAccountSetupRepository.save(clientAccountSetup);

		//gemini headline description creation
		String userInput = "Generate exactly 100 Headlines, 100 Long Headlines, and 100 Descriptions for " + dto.getClientBusinessName() + " (Industry: " + dto.getIndustry() + ", Sub-Industry: " + dto.getSubIndustry() + ") for Google Search Ads. Headlines must be max 30 characters, Long Headlines max 90 characters, Descriptions max 90 characters. Do not include any special characters, emojis, or symbols except alphabets, numbers, and spaces. Strictly return the output in this format only:\n\n## " + dto.getClientBusinessName() + " - 100 Headlines (Max 30 Characters)\n\n1. Headline text\n2. Headline text\n...\n100. Headline text\n\n## " + dto.getClientBusinessName() + " - 100 Long Headlines (Max 90 Characters)\n\n1. Long Headline text\n2. Long Headline text\n...\n100. Long Headline text\n\n## " + dto.getClientBusinessName() + " - 100 Descriptions (Max 90 Characters)\n\n1. Description text\n2. Description text\n...\n100. Description text";
		GeminiResponse geminiResponse = geminiAIService.getGeminiResponse(userInput);
		if(geminiResponse == null) {
			throw new ResourceNotFoundException("Gemini Response is empty");
		}

		List<Candidate> candidates = geminiResponse.getCandidates(); 
		Candidate candidate = candidates.get(0); 
		Content content = candidate.getContent(); 
		Part part = content.getParts().get(0); 
		String text = part.getText();

		geminiAIService.handleGeminiResponse(dto.getClientId(), dto.getClientBusinessName(), text);

		//location details store in client location setup
		if(dto.getClientLocationSetupList() != null) {
			for (ClientLocationSetupDto locDTO : dto.getClientLocationSetupList()) {

				ClientLocationSetup entity = new ClientLocationSetup( 
						dto.getClientId(), // parent clientId
						locDTO.getDealerId(), locDTO.getLatitude(), locDTO.getLongitude(),
						locDTO.getRadius(), locDTO.getRadiusUnit(), locDTO.getCallAdPhoneNumber(),
						locDTO.getLandingPageUrl(), locDTO.getClientCampaignPhoneNumber()
						);
				clientLocationSetupRepository.save(entity);
			}
		}
	}
	
	//Client industry-subindustry and location details post call
	public void insertClientLocationsDetails(ClientLocationDetailsRequest clientLocationDetailsRequest) throws InterruptedException {
		//client account setup entry
		ClientAccountSetup clientAccountSetup = ClientAccountSetup.builder()
				.clientId(clientLocationDetailsRequest.getClientId())
				.industry(clientLocationDetailsRequest.getIndustry())
				.subIndustry(clientLocationDetailsRequest.getSubIndustry())
				.build();
			
		
		//get clientName 
		Client client = clientRepository.findByClientId(clientLocationDetailsRequest.getClientId()).orElse(null);
		
		//gemini headline description creation
				String userInput = "Generate exactly 100 Headlines and 100 Descriptions for " + client.getClientName() + " (Industry: " + clientLocationDetailsRequest.getIndustry() + ", Sub-Industry: " + clientLocationDetailsRequest.getSubIndustry() + ") for Google Search Ads. Headlines must be max 30 characters, Descriptions max 90 characters. Do not include any special characters, emojis, or symbols except alphabets, numbers, and spaces. Strictly return the output in this format only:\n\n## " + client.getClientName() + " - 100 Headlines (Max 30 Characters)\n\n1. Headline text\n2. Headline text\n...\n100. Headline text\n\n## " + client.getClientName() + " - 100 Descriptions (Max 90 Characters)\n\n1. Description text\n2. Description text\n...\n100. Description text";
				GeminiResponse geminiResponse = geminiAIService.getGeminiResponse(userInput);
				if(geminiResponse == null) {
					throw new ResourceNotFoundException("Gemini Response is empty");
				}

				List<Candidate> candidates = geminiResponse.getCandidates(); 
				Candidate candidate = candidates.get(0); 
				Content content = candidate.getContent(); 
				Part part = content.getParts().get(0); 
				String text = part.getText();

				geminiAIService.handleGeminiResponse(clientLocationDetailsRequest.getClientId(), client.getClientName(), text);
		
				
		//location details store in client location setup
				if(clientLocationDetailsRequest.getLocationDetailsRequest() != null) {
					for (LocationDetailsRequest loc : clientLocationDetailsRequest.getLocationDetailsRequest()) {
						ClientLocationSetup entity = new ClientLocationSetup(
								clientLocationDetailsRequest.getClientId(),
								loc.getDealerId(), loc.getLatitude(), loc.getLongitude(),
								loc.getRadius(), loc.getRadiusUnit(), loc.getCallAdPhoneNumber(),
								loc.getLandingPageUrl(), "");


						clientLocationSetupRepository.save(entity);
					}
				}
	}

	//client onboarding :: data required get call
	public PopulateClientOnboardingDetailsDto getClientOnboardingDetails(String clientId, String userId) {

		List<ClientIndustryDetails> industryDetails = clientIndustryDetailsRepository.findAll();

		Map<String, List<String>> industryVsSubIndustry = industryDetails.stream()
				.collect(Collectors.groupingBy(ClientIndustryDetails::getIndustry, 
						Collectors.mapping(ClientIndustryDetails::getSubIndustry, Collectors.toList())));

		//By client id get phone no and country code
		Client clientDetail = clientRepository.findByClientId(clientId).orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
		String countryCode = clientDetail.getCountryCode();
		String phoneNumber = clientDetail.getPhoneNumber();
		
		List<DealerLocation> location = dealerLocationService.getFilteredDealerLocationByUserId(clientId, userId);

		if(location == null || location.isEmpty()) {
			throw new ResourceNotFoundException("Locations not found");
		}

//		Map<String,String> dealerIdVsDealerName = location.stream().collect(Collectors.toMap(DealerLocation::getDealerId, DealerLocation::getDealerName));
		
		List<CampaignDealerLocationDetails> locationDetailsList = location
		        .stream()
		        .map(dealerLocation -> {
		        	CampaignDealerLocationDetails locationDetails = new CampaignDealerLocationDetails();
		            locationDetails.setDealerId(dealerLocation.getDealerId());
		            locationDetails.setName(dealerLocation.getDealerName());
		            locationDetails.setCity(dealerLocation.getCity());
		            locationDetails.setState(dealerLocation.getState());
		            locationDetails.setPincode(dealerLocation.getPincode());
		            return locationDetails;
		        })
		        .collect(Collectors.toList());
		
		List<String> dealerIds = location.stream().map(DealerLocation :: getDealerId).collect(Collectors.toList());
		List<String> radiusMetrics = Arrays.asList(GoogleCampaign.KILOMETERS, GoogleCampaign.MILES);
		//List<String> campaignType = Arrays.asList(GoogleCampaign.SEARCH_CAMPAIGN, GoogleCampaign.PMAX_CAMPAIGN, GoogleCampaign.CALL_AD_CAMPAIGN);

		return new PopulateClientOnboardingDetailsDto(industryVsSubIndustry, dealerIds, locationDetailsList, radiusMetrics, countryCode, phoneNumber, clientDetail.getClientName());
	}
	
	//Client Location details prefilled - getCall
	public ClientLocationDetailsDto getLocationDetails(String clientId, String dealerId) {
		
		Client client = clientRepository.findByClientId(clientId).orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
		String clientName = client.getClientName();

		DealerLocation location = dealerLocationRepository.findByClientIdAndDealerId(clientId, dealerId).orElseThrow(() -> new ResourceNotFoundException("Dealer location not found for client: " + clientId + " dealer: " + dealerId));
		
		ClientLocationDetailsDto clientLocationDetailsDto = new ClientLocationDetailsDto();
		List<String> statesList = new ArrayList<>();
		List<String> cityList = new ArrayList<>();
		
		if(location != null) {
			
			String state = location.getState();
			statesList.add(state);
			
			String city = location.getCity();
			cityList.add(city);
			
			String address = location.getAddress();
			String pincode = location.getPincode();
			
			clientLocationDetailsDto.setState(statesList);
			clientLocationDetailsDto.setCity(cityList);
			clientLocationDetailsDto.setAddress(address);
			clientLocationDetailsDto.setPincode(pincode);
			clientLocationDetailsDto.setLatitude(location.getLatitude());
			clientLocationDetailsDto.setLongitude(location.getLongitude());
			clientLocationDetailsDto.setDealerId(location.getDealerId());
		}
		
		return clientLocationDetailsDto;
	}

	//client campaign : get call	
	public PopulateClientLocationDetailsDto getClientLocationDetails(String clientId, String userId) {

		Client client = clientRepository.findByClientId(clientId).orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
		String clientName = client.getClientName();

		ClientAccountSetup clientAccountSetup = clientAccountSetupRepository.findByClientId(clientId)
				.orElseThrow(() -> new ResourceNotFoundException("Client account setup not found for client: " + clientId));

		List<ClientLocationSetup> clientLocationSetups = clientLocationSetupRepository.findByClientId(clientId);
		Map<String, ClientLocationSetup> campaignLocation = clientLocationSetups.stream().collect(Collectors.toMap(ClientLocationSetup::getDealerId, Function.identity()));

		List<DealerLocation> filteredLocations = dealerLocationService.getFilteredDealerLocationByUserId(clientId, userId);
		List<String> dealerLocations = filteredLocations.stream().map(DealerLocation::getDealerId).collect(Collectors.toList());

		String platform = clientAccountSetup.getPlatform();
		String youtubeVideoUrl = clientAccountSetup.getYoutubeVideoUrl();

		List<String> dealerIdList = new ArrayList<>();
		List<CampaignLocationDetails> campaignLocationDetails = new ArrayList<>();

		for(DealerLocation location : filteredLocations) {
			if(campaignLocation.containsKey(location.getDealerId())) {
				ClientLocationSetup campaignLocations = campaignLocation.get(location.getDealerId());
				CampaignLocationDetails locationDetails = new CampaignLocationDetails(location.getDealerId(), location.getDealerName(), campaignLocations.getLandingPageUrl(), true);
				campaignLocationDetails.add(locationDetails);
			}else {
				CampaignLocationDetails locationDetails = new CampaignLocationDetails(location.getDealerId(), location.getDealerName(), "", false);
				campaignLocationDetails.add(locationDetails);
			}
		}

		campaignLocationDetails.sort(Comparator.comparing(CampaignLocationDetails::isHasData).reversed());

		PopulateClientLocationDetailsDto responseDto = new PopulateClientLocationDetailsDto();
		responseDto.setClientId(clientId);
		responseDto.setClientName(clientName);
		responseDto.setPlatform(platform);
		responseDto.setYoutubeVideoUrl(youtubeVideoUrl);
		responseDto.setCampaignLocationDetails(campaignLocationDetails);

		return responseDto;
	}

	//internal client onboarding get keywords
	public PopulateClientKeywordDetailsDto getClientKeywordDetails() {
		//MAPS TO POPULATE
		Map<String, ClientAccountSetup> clientIdVsClientAccountSetup = new HashMap<>();
		Map<String, List<ClientLocationSetup>> clientIdVsClientLocationSetup = new HashMap<>();

		//FETCH DATA
		List<GoogleAccount> allGoogleAccount = accountRepository.findAll();
		List<Client> allClients = clientRepository.findAll();

		//Get only clients whose account is NOT generated
		Set<String> clientsWithAccount = allGoogleAccount.stream().map(GoogleAccount::getAccountName).collect(Collectors.toSet());

		List<ClientData> clientList = allClients.stream().filter(c -> !clientsWithAccount.contains(c.getClientName().trim())).map(c -> new ClientData(
				c.getClientName(), c.getClientId())).collect(Collectors.toList());

		//Load Account Setup & Location Setup
		List<ClientAccountSetup> clientAccountSetups = clientAccountSetupRepository.findAll();
		List<ClientLocationSetup> clientLocationSetups = clientLocationSetupRepository.findAll();

		//POPULATE MAP ClientAccountSetup and ClientLocationSetup
		clientAccountSetups.forEach(a -> clientIdVsClientAccountSetup.put(a.getClientId(), a));
		clientLocationSetups.forEach(l -> {clientIdVsClientLocationSetup.computeIfAbsent(l.getClientId(), k -> new ArrayList<>()).add(l);});

		//SET DTO FOR RESPONSE
		PopulateClientKeywordDetailsDto responseDto = new PopulateClientKeywordDetailsDto();

		responseDto.setClientNames(clientList);

		List<ClientKeywordsDTO> list = new ArrayList<>();
		for(ClientData client : clientList) {
			String clientId = client.getClientId();

			//GET OBJECT ONLY FOR CLIENT FOR WHICH ACCOUNT ID NOT GENERATED
			ClientAccountSetup accountSetup = clientIdVsClientAccountSetup.get(clientId);
			List<ClientLocationSetup> locations = clientIdVsClientLocationSetup.get(clientId);

			if (accountSetup == null || locations == null) continue;

			String subIndustry = accountSetup.getSubIndustry();
			List<BaseKeywords> subIndustryKeywordsList = baseKeywordsRepository.getBySourceValue(subIndustry);

			Map<String, List<BaseKeywords>> subIndustryKeywordMap = new HashMap<>();
			subIndustryKeywordMap.put(subIndustry, subIndustryKeywordsList);

			Map<String, List<BaseKeywords>> urlKeywordMap = new HashMap<>();

			for (ClientLocationSetup loc : locations) {
				String url = loc.getLandingPageUrl();
				List<BaseKeywords> keywords = baseKeywordsRepository.getBySourceValue(url);
				urlKeywordMap.put(url, keywords);
			}

			ClientKeywordsDTO dto = new ClientKeywordsDTO();

			dto.setClientId(client.getClientId());
			dto.setSubIndustryKeywords(subIndustryKeywordMap);
			dto.setUrlKeywords(urlKeywordMap);
			list.add(dto);
		}

		responseDto.setClientKeywords(list);
		return responseDto;
	}

	//internal client onboarding : post call
	public void insertGoogleAccountAndClientDataSetupKeyword(ClientOnboardingDetailsDto dto) {

		String clientId = dto.getClientId();
		Client client = clientRepository.findByClientId(dto.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found: " + dto.getClientId()));
		String clientName = client.getClientName();

		String googleAccountId = dto.googleAccountId;

		GoogleAccount googleAccount = new GoogleAccount();
		googleAccount.setAccountName(clientName);
		googleAccount.setClientId(clientId);
		googleAccount.setLoginCustomerId(dto.getLoginCustomerId());
		googleAccount.setAccountId(googleAccountId);
		googleAccount.setProcessHistory(false);
		googleAccount.setLastModifiedDate(new Date());
		googleAccount.setLastModifiedBy("");

		accountRepository.save(googleAccount);

		List<BaseKeywords>subIndustryKeywords = dto.getSubIndustryKeywords();

		if(subIndustryKeywords != null  && !subIndustryKeywords.isEmpty()) {
			for(BaseKeywords baseKeyword : subIndustryKeywords) {
				ClientDataSetupKeywords clientDataSetupKeywords =  new ClientDataSetupKeywords();
				clientDataSetupKeywords.setClientId(dto.getClientId());
				clientDataSetupKeywords.setKeyword(baseKeyword.getKeyword().trim());
				clientDataSetupKeywords.setSearchVolume(baseKeyword.getSearchVolume());
				clientDataSetupKeywords.setSource(BaseKeywords.SOURCE_SUB_INDUSTRY);
				clientDataSetupKeywordsRepository.save(clientDataSetupKeywords);
			}
		}

		List<BaseKeywords>urlKeywords = dto.getUrlKeywords();

		if(urlKeywords != null && !urlKeywords.isEmpty()) {
			for(BaseKeywords baseKeyword : urlKeywords) {
				ClientDataSetupKeywords clientDataSetupKeywords =  new ClientDataSetupKeywords();
				clientDataSetupKeywords.setClientId(dto.getClientId());
				clientDataSetupKeywords.setKeyword(baseKeyword.getKeyword().trim());
				clientDataSetupKeywords.setSearchVolume(baseKeyword.getSearchVolume());
				clientDataSetupKeywords.setSource(BaseKeywords.SOURCE_URL);
				clientDataSetupKeywordsRepository.save(clientDataSetupKeywords);
			}
		}
	}

	//get call to show call campaigns to client
	public ViewPageableCampaignResponse viewAllCampaigns(CampaignFilterRequest request) {

		log.info("[viewAllCampaigns] clientId={} pageNo={} search={}", request.getClientId(), request.getPageNo(), request.getSearch());

		LocationFilterRequest locationRequest = locationFilterRequestCreate(request);
		List<DealerLocation> dealerLocation = dealerLocationService.getFilteredDealerLocation(locationRequest);
		Map<String,String> dealerIdVsDealerNameMap = dealerLocation.stream().collect(Collectors.toMap(DealerLocation :: getDealerId, DealerLocation :: getDealerName));
		List<String> location = dealerLocation.stream().map(DealerLocation :: getDealerId).collect(Collectors.toList());

		Pageable pageable = PageRequest.of(request.getPageNo(), 10);
		Page<GoogleCampaign> page;
		if (request.getSearch() == null || request.getSearch().trim().isEmpty()) {
			page = googleCampaignRepository.findByClientIdAndDealerIdInOrderByIdDesc(request.getClientId(), location, pageable);
		} else {
			page = googleCampaignRepository.findByClientIdAndDealerIdInAndCampaignNameContainingIgnoreCaseOrderByIdDesc(request.getClientId(), location, request.getSearch(), pageable);
		}

		List<ViewAllCampaignsResponse> viewAllCampaignsResponseList = new ArrayList<>();
		for(GoogleCampaign campaign : page) {
			if(dealerIdVsDealerNameMap.containsKey(campaign.getDealerId())) {
			String dealerName = dealerIdVsDealerNameMap.get(campaign.getDealerId());
			ViewAllCampaignsResponse viewAllCampaignsResponse = new ViewAllCampaignsResponse(campaign.getClientId(), campaign.getId(),campaign.getCampaignName(), campaign.getStartDate(), campaign.getEndDate(), campaign.getTotalBudget(), campaign.getPlatform(), campaign.getStatus(), dealerName+"-"+campaign.getDealerId());
			
			viewAllCampaignsResponseList.add(viewAllCampaignsResponse);
			}
		}
		ViewPageableCampaignResponse response = new ViewPageableCampaignResponse(viewAllCampaignsResponseList, page.getTotalPages(), page.getTotalElements());

		return response;
	}

	//get call to show call campaigns to coe
	public ViewPageableCampaignResponse viewAllCoeCampaigns(String search, int pageNo) {

		Pageable pageable = PageRequest.of(pageNo, 10);
		Page<GoogleCampaign> page;
		if (search == null || search.trim().isEmpty()) {
			page = googleCampaignRepository.findByStatusOrderByIdDesc(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAYMENT_SUCCESSFUL, pageable);
		} else {
			page = googleCampaignRepository.findByStatusAndCampaignNameContainingIgnoreCaseOrderByIdDesc(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAYMENT_SUCCESSFUL, search, pageable);
		}

		Set<String> pagesDealerIds = page.getContent().stream().map(GoogleCampaign::getDealerId).collect(Collectors.toSet());
		Map<String, String> dealerNameMapCoe = dealerLocationRepository.findByDealerIdIn(pagesDealerIds).stream()
				.collect(Collectors.toMap(
						d -> d.getClientId() + "_" + d.getDealerId(),
						DealerLocation::getDealerName,
						(a, b) -> a));
		List<ViewAllCampaignsResponse> viewAllCampaignsResponseList = new ArrayList<>();
		for(GoogleCampaign campaign : page) {
			String dealerName = dealerNameMapCoe.get(campaign.getClientId() + "_" + campaign.getDealerId());
			if (dealerName != null) {
			ViewAllCampaignsResponse viewAllCampaignsResponse = new ViewAllCampaignsResponse(campaign.getClientId(), campaign.getId(),campaign.getCampaignName(), campaign.getStartDate(), campaign.getEndDate(), campaign.getTotalBudget(), campaign.getPlatform(), campaign.getStatus(), dealerName+"-"+campaign.getDealerId());
			viewAllCampaignsResponseList.add(viewAllCampaignsResponse);
			}
		}
		ViewPageableCampaignResponse response = new ViewPageableCampaignResponse(viewAllCampaignsResponseList, page.getTotalPages(), page.getTotalElements());
		return response;
	}

	//get campaign setup details for coe
	public ViewCampaignSetupDetailsResponse viewCampaignSetupDetails(Long campaignId) {

		GoogleCampaign campaign = googleCampaignRepository.findById(campaignId).orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
		Client client = clientRepository.findByClientId(campaign.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found"));
		ClientAccountSetup clientAccountSetup = clientAccountSetupRepository.findByClientId(campaign.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client account setup not found"));
		ClientLocationSetup clientLocationSetup = clientLocationSetupRepository.findByClientIdAndDealerId(campaign.getClientId(), campaign.getDealerId()).orElseThrow(() -> new ResourceNotFoundException("Client location setup not found"));

		ClientData clientData = new ClientData(client.getClientName(), client.getClientId());
		List<ClientDataSetupKeywords> urlKeywords = clientDataSetupKeywordsRepository.findByClientIdAndSource(campaign.getClientId(), BaseKeywords.SOURCE_URL);
		List<ClientDataSetupKeywords> subIndustryKeywords = clientDataSetupKeywordsRepository.findByClientIdAndSource(campaign.getClientId(), BaseKeywords.SOURCE_SUB_INDUSTRY);

		//common list
		List<String> matchType = List.of(ViewCampaignSetupDetailsResponse.MATCH_TYPE_EXACT, ViewCampaignSetupDetailsResponse.MATCH_TYPE_PHRASE, ViewCampaignSetupDetailsResponse.MATCH_TYPE_BROAD);
		List<String> networks = List.of(ViewCampaignSetupDetailsResponse.MATCH_NETWORK_TARGET_GOOGLE_SEARCH, ViewCampaignSetupDetailsResponse.MATCH_NETWORK_TARGET_SEARCH_NETWORK);
		List<String> biddingStrategy = List.of(ViewCampaignSetupDetailsResponse.BIDDING_STRATERGY_MAXIMIZE_CLICKS, ViewCampaignSetupDetailsResponse.BIDDING_STRATERGY_MAXIMIZE_CONVERSIONS);

		String finalUrl = "";
		String adName = "";
		List<String> headlines = new ArrayList<>();
		List<String> descriptions = new ArrayList<>();

		ViewCampaignSetupDetailsResponse response;
		if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.SEARCH_CAMPAIGN)) {
			response = new SearchViewCampaignSetupDetailsResponse();
		} else if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.PMAX_CAMPAIGN)) {
			response = new PmaxViewCampaignSetupDetailsResponse();
		}else if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.CALL_AD_CAMPAIGN)) {
			response = new CallAdViewCampaignSetupDetailsResponse();
		}else {
			response = new ViewCampaignSetupDetailsResponse();
		}

		response.setClientDate(clientData);
		response.setCampaignId(campaign.getId());
		response.setCampaignName(campaign.getCampaignName());
		response.setClientComment(campaign.getClientComment());
		response.setStartDate(campaign.getStartDate());
		response.setEndDate(campaign.getEndDate());
		response.setPlatform(campaign.getPlatform());
		response.setAdPhoneNumber(campaign.getAdPhoneNumber());
		response.setMatchType(matchType);
		response.setNetwork(networks);
		response.setBiddingStrategy(biddingStrategy);
		response.setIndustry(clientAccountSetup.getIndustry());
		response.setSubIndustry(clientAccountSetup.getSubIndustry());
		response.setClientLocationSetup(clientLocationSetup);
		response.setDailyBudget(campaign.getDailyBudget());
		response.setTotalBudget(campaign.getTotalBudget());
		response.setUrlKeywords(urlKeywords);
		response.setSubIndustryKeywords(subIndustryKeywords);

		if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.SEARCH_CAMPAIGN)) {

			GoogleAdgroup adGroup = googleAdgroupRepository.findFirstByCampaignId(campaignId).orElseThrow(() -> new ResourceNotFoundException("google ad group not found"));
			SearchViewCampaignSetupDetailsResponse searchResponse = (SearchViewCampaignSetupDetailsResponse) response;
			List<GoogleResponsiveAd> responsiveAd = googleResponsiveAdRepository.findAllByAdgroupId(adGroup.getId());
			if(responsiveAd.isEmpty()) {
				throw new InvalidRequestException("google responsive ad not found");
			}

			for(GoogleResponsiveAd ad : responsiveAd) {

				if(ad.getType().equalsIgnoreCase(GoogleResponsiveAd.FINAL_URL)) {
					finalUrl = ad.getValue();
				}

				if(ad.getType().equalsIgnoreCase(GoogleResponsiveAd.HEADLINE)) {
					headlines.add(ad.getValue());
				}

				if(ad.getType().equalsIgnoreCase(GoogleResponsiveAd.DESCRIPTION)){
					descriptions.add(ad.getValue());
				}

				if(ad.getType().equalsIgnoreCase(GoogleResponsiveAd.AD_NAME)){
					adName = ad.getValue();
				}
			}

			response.setFinalUrl(finalUrl);
			response.setHeadlines(headlines);
			response.setDescriptions(descriptions);	

			searchResponse.setAdName(adName);
			return searchResponse;
		}
		if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.CALL_AD_CAMPAIGN)) {

			GoogleAdgroup adGroup = googleAdgroupRepository.findFirstByCampaignId(campaignId).orElseThrow(() -> new ResourceNotFoundException("google ad group not found"));
			CallAdViewCampaignSetupDetailsResponse callResponse = (CallAdViewCampaignSetupDetailsResponse) response;
			GoogleCallAd callAd = googleCallAdRepository.findByAdGroupId(adGroup.getId()).orElseThrow(() -> new ResourceNotFoundException("call ad not found"));
			response.setFinalUrl(callAd.getFinalUrl());

			headlines.add(callAd.getHeadline1());
			headlines.add(callAd.getHeadline2());
			response.setHeadlines(headlines);	

			descriptions.add(callAd.getDescription1());
			descriptions.add(callAd.getDescription2());
			response.setDescriptions(descriptions);

			callResponse.setPath1(callAd.getPath1());
			callResponse.setPath2(callAd.getPath2());

			return callResponse;
		}
		if(campaign.getPlatform().equalsIgnoreCase(GoogleCampaign.PMAX_CAMPAIGN)) {

			List<String> longHeadlines = new ArrayList<>();
			List<String> marketingImages = new ArrayList<>();
			List<String> squareMarketingImages = new ArrayList<>();
			List<String> portrainMarketingImages = new ArrayList<>();
			List<String> logos = new ArrayList<>();
			List<String> landscapeLogos = new ArrayList<>();
			String businessName = "";

			GoogleCampaignAssetGroup assetGroup = googleCampaignAssetGroupRepository.findByCampaignId(campaignId).orElseThrow(() -> new ResourceNotFoundException("campaign asset group not found"));
			PmaxViewCampaignSetupDetailsResponse pmaxResponse = (PmaxViewCampaignSetupDetailsResponse) response;

			List<GoogleCampaignAssets> assets = googleCampaignAssetsRepository.findByAssetGroupId(assetGroup.getId());
			if(assets.isEmpty()) {
				throw new InvalidRequestException("pmax assets not found");
			}

			for(GoogleCampaignAssets asset : assets) {
				if(asset.getType().equalsIgnoreCase(AssetFieldType.HEADLINE.toString())) {
					headlines.add(asset.getValue());
				}
				if(asset.getType().equalsIgnoreCase(AssetFieldType.DESCRIPTION.toString())) {
					descriptions.add(asset.getValue());
				}
				if(asset.getType().equalsIgnoreCase(AssetFieldType.LONG_HEADLINE.toString())) {
					longHeadlines.add(asset.getValue());
				}
				if(asset.getType().equalsIgnoreCase(AssetFieldType.MARKETING_IMAGE.toString())) {
					marketingImages.add(asset.getValue());
				}
				if(asset.getType().equalsIgnoreCase(AssetFieldType.SQUARE_MARKETING_IMAGE.toString())) {
					squareMarketingImages.add(asset.getValue());
				}
				if(asset.getType().equalsIgnoreCase(AssetFieldType.PORTRAIT_MARKETING_IMAGE.toString())) {
					portrainMarketingImages.add(asset.getValue());
				}
				if(asset.getType().equalsIgnoreCase(AssetFieldType.LOGO.toString())) {
					logos.add(asset.getValue());
				}
				if(asset.getType().equalsIgnoreCase(AssetFieldType.LANDSCAPE_LOGO.toString())) {
					landscapeLogos.add(asset.getValue());
				}
				if(asset.getType().equalsIgnoreCase(AssetFieldType.BUSINESS_NAME.toString())) {
					businessName = asset.getValue();
				}
			}

			pmaxResponse.setFinalUrl(assetGroup.getFinalUrl());
			response.setHeadlines(headlines);
			response.setDescriptions(descriptions);	

			pmaxResponse.setLongHeadlines(longHeadlines);
			pmaxResponse.setMarketingImages(marketingImages);
			pmaxResponse.setSquareMarketingImages(squareMarketingImages);
			pmaxResponse.setPortraitMarketingImages(portrainMarketingImages);
			pmaxResponse.setLogo(logos);
			pmaxResponse.setLandscapeLogo(landscapeLogos);
			pmaxResponse.setBusinessName(businessName);

			return pmaxResponse;
		}

		return response;
	}

	//reporting methods start
	public ClientDataSetup findClientDataSetupByClientId(String clientId) {
		return clientDataSetupRepository.findClientDataSetupByClientId(clientId);
	}

	public List<GoogleCampaign> findAllGoogleCampaignByClientIdAndDealerIds(String clientId, List<String> dealerIds){
		return googleCampaignRepository.findByClientIdAndDealerIdIn(clientId, dealerIds);
	}

	public List<ArtAdwordsData> getArtAdwordsData(long customerId, Date segmentsStartDate, Date segmentsEndDate) throws IOException, JobException, InterruptedException, ParseException, SQLException {
		return artAdwordsDataBQRepository.getArtAdwordsData(customerId, segmentsStartDate, segmentsEndDate);
	}
	//reporting methods end

	//resume campaign
	public SelfServeResponse resumeCampaign(String clientId, String userId, Long campaignId, String comment) {

		GoogleCampaign campaign = googleCampaignRepository.findById(campaignId).orElseThrow(() -> new ResourceNotFoundException("campaign not found"));

		List<UserClientLocMapping> mapping = userClientLocMappingRepository.findByUserIdAndclientId(userId, clientId);
		List<String> mappedDealerIds = mapping.stream().map(UserClientLocMapping::getDealerId).collect(Collectors.toList());

		if (mappedDealerIds.contains(campaign.getDealerId())) {
			campaign.setComment(comment);
			campaign.setStatus(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_LIVE);
			googleCampaignRepository.save(campaign);
			return new SelfServeResponse(SelfServeResponse.RESULT_SUCCESS, "Campaign Resumed", GoogleCampaign.ROLE_CLIENT, campaign.getId());
		} else {
			throw new InvalidRequestException("User not allowed to resume this campaign");
		}
	}

	//pause campaign
	public SelfServeResponse pauseCampaign(String clientId, String userId, Long campaignId, String comment) {

		GoogleCampaign campaign = googleCampaignRepository.findById(campaignId).orElseThrow(()-> new ResourceNotFoundException("campaign not found"));

		List<UserClientLocMapping> mapping = userClientLocMappingRepository.findByUserIdAndclientId(userId, clientId);
		List<String> mappedDealerIds = mapping.stream().map(UserClientLocMapping::getDealerId).collect(Collectors.toList());

		if(mappedDealerIds.contains(campaign.getDealerId())) {

			String customerId = campaign.getGoogleAccountID();
			GoogleAccount account = accountRepository.findByClientId(clientId);
			if (account == null) {
				throw new ResourceNotFoundException("Google account not found for client: " + clientId);
			}
			long loginCustomerId = account.getLoginCustomerId();
			String campaignResourceName = campaign.getCampaignResourceName();
			//GoogleAdsClient googleAdsClient = GoogleSessionFactory.getGoogleAdsClientByLoginCustomerID(loginCustomerId);
			//searchCampaignApi.pauseCampaign(googleAdsClient, customerId, campaignResourceName, loginCustomerId);

			campaign.setComment(comment);
			campaign.setStatus(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAUSED);
			googleCampaignRepository.save(campaign);;
			return new SelfServeResponse(SelfServeResponse.RESULT_SUCCESS, "Campaign Paused", GoogleCampaign.ROLE_CLIENT, campaign.getId());

		}else {
			throw new InvalidRequestException("User not allowed to pause this campaign");
		}
	}

	public List<GoogleCampaign> findByClientIdAndDealerIdInAndStartDateBetween(String clientId, Set<String>dealerIds, Date fromDate, Date toDate){
	
		List<GoogleCampaign> googleCampaings = googleCampaignRepository.findByClientIdAndDealerIdInAndStartDateBetween(clientId, dealerIds, fromDate, toDate);
		
		return googleCampaings;
	}
	
	public List<GoogleCampaign> findAllGoogleCampaignByStatus(String status){
		return googleCampaignRepository.findAllGoogleCampaignByStatus(status);
	}

	public IndustryDetailsDto getIndustryDetails() {

	    List<ClientIndustryDetails> industryDetails =
	            clientIndustryDetailsRepository.findAll();

	    Map<String, List<String>> industryMap = new HashMap<>();

	    for (ClientIndustryDetails detail : industryDetails) {
	        industryMap
	            .computeIfAbsent(detail.getIndustry(), k -> new ArrayList<>())
	            .add(detail.getSubIndustry());
	    }

	    return new IndustryDetailsDto(industryMap);
	}
	/*public Double sumDeliveredImpressionsByCampaignResourceName(List<String>campaignResourceNames) {
		return googleCampaignRepository.sumDeliveredImpressionsByCampaignResourceName(campaignResourceNames);
	}*/
}
