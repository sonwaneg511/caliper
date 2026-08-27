package com.caliper.location.service;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.caliper.campaign.google.entity.ClientAccountSetup;
import com.caliper.campaign.google.entity.ClientLocationSetup;
import com.caliper.campaign.google.repository.ClientAccountSetupRepository;
import com.caliper.campaign.google.repository.ClientLocationSetupRepository;
import com.caliper.location.dto.CampaignSettings;
import com.caliper.location.dto.CampaignSettingsDto;
import com.caliper.location.dto.FacebookDetailsDto;
import com.caliper.location.dto.GmbDetailsDto;
import com.caliper.location.dto.LocationDetailsDto;
import com.caliper.location.dto.LocationOverviewDto;
import com.caliper.location.dto.request.CampaignSettingsBody;
import com.caliper.location.dto.request.FacebookDetailsBody;
import com.caliper.location.dto.request.GMBDetailsBody;
import com.caliper.location.dto.request.InfoRequestDto;
import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.dto.request.LocationOverviewBody;
import com.caliper.location.dto.response.AccountVerifyResponseDto;
import com.caliper.location.dto.response.CategoryDetails;
import com.caliper.location.dto.response.CategoryDto;
import com.caliper.location.dto.response.DealerList;
import com.caliper.location.dto.response.FacebookDealerList;
import com.caliper.location.dto.response.GMBDealerList;
import com.caliper.location.dto.response.LocationMediaDto;
import com.caliper.location.dto.response.ViewAllLocations;
import com.caliper.location.dto.response.ViewAllLocationsResponse;
import com.caliper.images.entity.LocationImage;
import com.caliper.images.entity.LocationImageMap;
import com.caliper.images.service.LocationImageService;
import com.caliper.location.entity.Client;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.entity.DealerOperationHours;
import com.caliper.location.facebook.dto.FacebookInfoRequest;
import com.caliper.location.facebook.entity.FacebookCategory;
import com.caliper.location.facebook.entity.FacebookLocation;
import com.caliper.location.facebook.repository.FacebookCategoryRepository;
import com.caliper.location.facebook.service.FacebookInfoQueueService;
import com.caliper.location.facebook.service.FacebookLocationService;
import com.caliper.location.gmb.dto.AttributeResult;
import com.caliper.location.gmb.dto.GMBInfoRequest;
import com.caliper.location.gmb.dto.HoursOfOperationRequest;
import com.caliper.location.gmb.dto.HoursOfOperationRequest.GMBDay;
import com.caliper.location.gmb.entity.GMBCategory;
import com.caliper.location.gmb.entity.GMBCategoryServices;
import com.caliper.location.gmb.entity.GMBInfoQueue;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.entity.GMBOperationHours;
import com.caliper.location.gmb.repository.GMBCategoryRepository;
import com.caliper.location.gmb.service.GBPCompletenessScoreCalculator;
import com.caliper.location.gmb.service.GMBCategoryServicesService;
import com.caliper.location.gmb.service.GMBHelper;
import com.caliper.location.gmb.service.GMBInfoQueueService;
import com.caliper.location.gmb.service.GMBLocationService;
import com.caliper.location.gmb.service.GMBSessionFactory;
import com.caliper.location.repository.ClientRepository;
import com.caliper.location.repository.DealerLocationRepository;
import com.caliper.usermanagement.dto.UserDetails;
import com.caliper.usermanagement.entity.UserClientLocMapping;
import com.caliper.usermanagement.repository.UserClientLocMappingRepository;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.mybusinessaccountmanagement.v1.MyBusinessAccountManagement;
import com.google.api.services.mybusinessaccountmanagement.v1.model.Account;
import com.google.api.services.mybusinessaccountmanagement.v1.model.ListAccountsResponse;
import com.google.api.services.mybusinessbusinessinformation.v1.MyBusinessBusinessInformation;
import com.google.api.services.mybusinessbusinessinformation.v1.model.Attribute;
import com.google.api.services.mybusinessbusinessinformation.v1.model.Attributes;
import com.google.api.services.mybusinessbusinessinformation.v1.model.BusinessHours;
import com.google.api.services.mybusinessbusinessinformation.v1.model.Categories;
import com.google.api.services.mybusinessbusinessinformation.v1.model.Category;
import com.google.api.services.mybusinessbusinessinformation.v1.model.FreeFormServiceItem;
import com.google.api.services.mybusinessbusinessinformation.v1.model.Label;
import com.google.api.services.mybusinessbusinessinformation.v1.model.Location;
import com.google.api.services.mybusinessbusinessinformation.v1.model.PhoneNumbers;
import com.google.api.services.mybusinessbusinessinformation.v1.model.Profile;
import com.google.api.services.mybusinessbusinessinformation.v1.model.ServiceItem;
import com.google.api.services.mybusinessbusinessinformation.v1.model.StructuredServiceItem;
import com.google.api.services.mybusinessbusinessinformation.v1.model.TimeOfDay;
import com.google.api.services.mybusinessbusinessinformation.v1.model.TimePeriod;
import com.google.api.services.mybusinessbusinessinformation.v1.model.UriAttributeValue;
import com.google.api.services.mybusinessplaceactions.v1.MyBusinessPlaceActions;
import com.google.api.services.mybusinessplaceactions.v1.model.PlaceActionLink;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.transaction.Transactional;

@Service
public class LocationService {

	@Autowired
	private GMBInfoQueueService gmbInfoQueueService;

	@Autowired
	private GBPCompletenessScoreCalculator gbpCompletenessScoreCalculator;

	@Autowired 
	private FacebookInfoQueueService facebookInfoQueueService;

	@Autowired
	private DealerLocationRepository dealerLocationRepository;

	@Autowired
	private DealerLocationService dealerLocationService;

	@Autowired 
	private GMBCategoryRepository gmbCategoryRepository;

	@Autowired
	private FacebookCategoryRepository facebookCategoryRepository;

	@Autowired
	private GMBLocationService gmbLocationService;

	@Autowired
	private FacebookLocationService facebookLocationService;

	@Autowired
	private GMBSessionFactory gmbSessionFactory;

	@Autowired
	private GMBCategoryServicesService categoryServicesService;

	@Autowired
	private GMBHelper gmbHelper;

	@Autowired
	private ClientAccountSetupRepository clientAccountSetupRepository;

	@Autowired
	private ClientLocationSetupRepository clientLocationSetupRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private UserClientLocMappingRepository userClientLocMappingRepository;

	@Autowired
	private LocationImageService locationImageService;

	public Set<String> getLocationsMappedToUser(String clientId, String userId) {

		List<UserClientLocMapping> mappings =
				userClientLocMappingRepository.findByUserIdAndclientId(userId, clientId);

		return mappings.stream()
				.map(UserClientLocMapping::getDealerId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}
	public CampaignSettings fetchCampaignSettingsByDealerIdAndClientId(String dealerId, String clientId, String userId) {

		Client client = clientRepository.findByClientId(clientId)
				.orElseThrow(() -> new ResourceNotFoundException("Client not found for id: " + clientId));

		ClientAccountSetup clientAccountSetup = clientAccountSetupRepository.findByClientId(clientId)
				.orElseThrow(() -> new ResourceNotFoundException("ClientAccountSetup not found for client id: " + clientId));

		ClientLocationSetup clientLocationSetup = clientLocationSetupRepository.findByClientIdAndDealerId(clientId, dealerId)
				.orElseThrow(() -> new ResourceNotFoundException("ClientLocationSetup not found for client id: " + clientId + " and dealer id: " + dealerId));

		return new CampaignSettings(
				client.getClientName(),
				clientAccountSetup.getIndustry(),
				clientAccountSetup.getSubIndustry(),
				clientLocationSetup.getRadius(),
				clientLocationSetup.getRadiusUnit(),
				client.getEmail(),
				clientLocationSetup.getClientCampaignPhoneNumber(),
				clientLocationSetup.getAdPhoneNumber(),
				clientLocationSetup.getLandingPageUrl(),
				clientAccountSetup.getYoutubeVideoUrl(),
				clientAccountSetup.getPlatform()
				);
	}
	//clean this method. make it modularize. create separate methods for gmb and fb request info also for operations hours
	public void insertRequestIntoInfoQueue(String dealerId, String clientId, String userId, InfoRequestDto request) {
		//======================================== GMB ============================================

		GMBInfoRequest gmbInfoRequest = new GMBInfoRequest();
		FacebookInfoRequest fbInfoRequest = new FacebookInfoRequest();

		DealerLocation dealerLocation = dealerLocationRepository.getDealerLocationByDealerIdAndClientId(dealerId, clientId);
		//Campaign Settings 
		ClientAccountSetup clientAccountSetup = clientAccountSetupRepository.findByClientId(clientId)
				.orElseThrow(() -> new ResourceNotFoundException("ClientAccountSetup not found for client id: " + clientId));

		ClientLocationSetup clientLocationSetup = clientLocationSetupRepository.findByClientIdAndDealerId(clientId, dealerId)
				.orElseThrow(() -> new ResourceNotFoundException("ClientLocationSetup not found for client id: " + clientId + " and dealer id: " + dealerId));
		
		if (request.getIndustry() != null && !request.getIndustry().isEmpty()) {
			clientAccountSetup.setIndustry(request.getIndustry());
		}
		if (request.getSubIndustry() != null && !request.getSubIndustry().isEmpty()) {
			clientAccountSetup.setSubIndustry(request.getSubIndustry());
		}
		if (request.getRadius() != null) {
			clientLocationSetup.setRadius(request.getRadius());
		}
		if (request.getClientCampaignPhoneNumber() != null && !request.getClientCampaignPhoneNumber().isEmpty()) {
			clientLocationSetup.setClientCampaignPhoneNumber(request.getClientCampaignPhoneNumber());
		}
		if (request.getCallAdsPhoneNumber() != null && !request.getCallAdsPhoneNumber().isEmpty()) {
			clientLocationSetup.setAdPhoneNumber(request.getCallAdsPhoneNumber());
		}
		if (request.getLandingPageUrl() != null && !request.getLandingPageUrl().isEmpty()) {
			clientLocationSetup.setLandingPageUrl(request.getLandingPageUrl());
		}
		if (request.getYoutubeUrl() != null && !request.getYoutubeUrl().isEmpty()) {
			clientAccountSetup.setYoutubeVideoUrl(request.getYoutubeUrl());
		}
		clientLocationSetupRepository.save(clientLocationSetup);
		clientAccountSetupRepository.save(clientAccountSetup);
//---------------------------------------------------------------------------------------------------------------------	
		// GMB primary and additional category
		if (request.getGmbPrimaryCategory() != null) {
			Long gmbPrimaryCategoryId = request.getGmbPrimaryCategory();
			GMBCategory gmbCategory = gmbCategoryRepository.findById(gmbPrimaryCategoryId)
					.orElseThrow(() -> new ResourceNotFoundException("GMB category not found: " + gmbPrimaryCategoryId));

			gmbInfoRequest.gmbPrimaryCategory = gmbCategory;
			dealerLocation.setPrimaryCategoryGMB(gmbPrimaryCategoryId);
		}

		if(request.getGmbAdditionalCategories() != null && !request.getGmbAdditionalCategories().isEmpty()) {
			List<Long> gmbAdditionCategoryIds = request.getGmbAdditionalCategories();
			List<GMBCategory> gmbAdditionalCategories = new ArrayList<>();
			for(Long addCategoryId :gmbAdditionCategoryIds) {
				GMBCategory gmbCat = gmbCategoryRepository.findById(addCategoryId)
						.orElseThrow(() -> new ResourceNotFoundException("GMB category not found: " + addCategoryId));

				gmbAdditionalCategories.add(gmbCat);
			}
			gmbInfoRequest.gmbAdditionalCategories = gmbAdditionalCategories;

			String gmbAdditionCategories = gmbAdditionCategoryIds.stream()
					.map(cat -> String.valueOf(cat))
					.collect(Collectors.joining(","));
			dealerLocation.setAdditionalCategoriesGMB(gmbAdditionCategories);
		}
		if (request.getAppointmentLink() != null && !request.getAppointmentLink().isEmpty()) {
			gmbInfoRequest.appointmentLink = request.getAppointmentLink();
		}
		if (request.getLanguageCode() != null && !request.getLanguageCode().isEmpty()) {
			gmbInfoRequest.languageCode = request.getLanguageCode();
		}
		if (request.getCategoryServices() != null) {
			gmbInfoRequest.categoryServices = request.getCategoryServices();
		}
		if (request.getInstagramAttribute() != null && !request.getInstagramAttribute().isEmpty()) {
			gmbInfoRequest.instagramAttribute = request.getInstagramAttribute();
		}
		if (request.getFacebookAttribute() != null && !request.getFacebookAttribute().isEmpty()) {
			gmbInfoRequest.facebookAttribute = request.getFacebookAttribute();
		}
		if (request.getYoutubeAttribute() != null && !request.getYoutubeAttribute().isEmpty()) {
			gmbInfoRequest.youtubeAttribute = request.getYoutubeAttribute();
		}
		if (request.getTwitterAttribute() != null && !request.getTwitterAttribute().isEmpty()) {
			gmbInfoRequest.twitterAttribute = request.getTwitterAttribute();
		}
		if (request.getWhatsAppAttribute() != null && !request.getWhatsAppAttribute().isEmpty()) {
			gmbInfoRequest.whatsAppAttribute = request.getWhatsAppAttribute();
		}
		if (request.getLinkedinAttribute() != null && !request.getLinkedinAttribute().isEmpty()) {
			gmbInfoRequest.linkedinAttribute = request.getLinkedinAttribute();
		}
		if (request.getLocationTitle() != null && !request.getLocationTitle().isEmpty()) {
			gmbInfoRequest.locationTitle = request.getLocationTitle();
		}
		if (request.getWebsiteUrl() != null && !request.getWebsiteUrl().isEmpty()) {
			gmbInfoRequest.websiteUrl = request.getWebsiteUrl();
		}
		if (request.getDescription() != null && !request.getDescription().isEmpty()) {
			gmbInfoRequest.description = request.getDescription();
		}
		if (request.getLabels() != null && !request.getLabels().isEmpty()) {
			gmbInfoRequest.labels = request.getLabels();
		}
		if (request.getOperationHours() != null) {
			gmbInfoRequest.operationHours = request.getOperationHours();
		}
		if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
			gmbInfoRequest.phoneNumber = request.getPhoneNumber();
		}
		if (request.getAdditionalPhones() != null && !request.getAdditionalPhones().isEmpty()) {
			gmbInfoRequest.additionalPhones = request.getAdditionalPhones();
		}

		//save gmbInfoRequest in GMBInfoQueue table
		gmbInfoQueueService.saveGMBInfoRequest(dealerId, gmbInfoRequest);
		//======================================== Facebook ===========================================================


		// FB Primary and additional Category

		if (request.getFbPrimaryCategory() != null) {

			Long fbPrimaryCategoryId = request.getFbPrimaryCategory();

			FacebookCategory fbPrimaryCategory = facebookCategoryRepository.findById(fbPrimaryCategoryId)
					.orElseThrow(() -> new ResourceNotFoundException("Facebook category not found: " + fbPrimaryCategoryId));

			fbInfoRequest.fbPrimaryCategory = fbPrimaryCategory;
			dealerLocation.setPrimaryCategoryFacebook(fbPrimaryCategoryId);
		}
		if(request.getFbAdditionalCategories() != null && !request.getFbAdditionalCategories().isEmpty()) {

			List<Long> fbAdditionCategoryIds = request.getFbAdditionalCategories();

			List<FacebookCategory> fbAdditionalCategories = new ArrayList<>();

			for (Long addCategoryId : fbAdditionCategoryIds) {
				FacebookCategory fbCat = facebookCategoryRepository.findById(addCategoryId)
						.orElseThrow(() -> new ResourceNotFoundException("Facebook category not found: " + addCategoryId));

				fbAdditionalCategories.add(fbCat);
			}
			fbInfoRequest.fbAdditionalCategories = fbAdditionalCategories;

			String fbAdditionCategories = fbAdditionalCategories.stream()
					.map(cat -> String.valueOf(cat.getId()))
					.collect(Collectors.joining(","));

			dealerLocation.setAdditionalCategoriesFacebook(fbAdditionCategories);
		}
		if (request.getArea() != null && !request.getArea().isEmpty()) {
			fbInfoRequest.area = request.getArea();
		}
		if (request.getStoreLocationDescriptor() != null && !request.getStoreLocationDescriptor().isEmpty()) {
			fbInfoRequest.StoreLocationDescriptor = request.getStoreLocationDescriptor();
		}
		if (request.getCity() != null && !request.getCity().isEmpty()) {
			fbInfoRequest.city = request.getCity();
		}
		if (request.getState() != null && !request.getState().isEmpty()) {
			fbInfoRequest.state = request.getState();
		}
		if (request.getPincode() != null && !request.getPincode().isEmpty()) {
			fbInfoRequest.pincode = request.getPincode();
		}
		if (request.getCountry() != null && !request.getCountry().isEmpty()) {
			fbInfoRequest.country = request.getCountry();
		}
		
		String address1 = dealerLocation.getAddress1();
		String address2 = dealerLocation.getAddress2();
		String address3 = dealerLocation.getAddress3();
		if (request.getAddress1() != null && !request.getAddress1().isEmpty()) {
			address1 = request.getAddress();
		}
		if (request.getAddress2() != null && !request.getAddress2().isEmpty()) {
			address2 = request.getAddress();
		}
		if (request.getAddress3() != null && !request.getAddress3().isEmpty()) {
			address3 = request.getAddress();
		}
		
		String address = address1 + address2 + address3;
		
		if (request.getAddress() != null && !request.getAddress().isEmpty()) {
			fbInfoRequest.address = address;
		}
		if (request.getLocationTitle() != null && !request.getLocationTitle().isEmpty()) {
			fbInfoRequest.locationTitle = request.getLocationTitle();
		}
		if (request.getWebsiteUrl() != null && !request.getWebsiteUrl().isEmpty()) {
			fbInfoRequest.websiteUrl = request.getWebsiteUrl();
		}
		if (request.getDescription() != null && !request.getDescription().isEmpty()) {
			fbInfoRequest.description = request.getDescription();
		}
		if (request.getLabels() != null && !request.getLabels().isEmpty()) {
			fbInfoRequest.labels = request.getLabels();
		}
		if (request.getAdditionalPhones() != null && !request.getAdditionalPhones().isEmpty()) {
			fbInfoRequest.additionalPhones = request.getAdditionalPhones();
		}
		if (request.getLatitude() != null) {
			fbInfoRequest.latitude = request.getLatitude();
		}
		if (request.getLongitude() != null) {
			fbInfoRequest.longitude = request.getLongitude();
		}
		if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
			fbInfoRequest.phoneNumber = request.getPhoneNumber();
		}
		if (request.getOperationHours() != null) {
			fbInfoRequest.operationHours = request.getOperationHours();
		}
		facebookInfoQueueService.saveFacebookInfoRequest(dealerId, fbInfoRequest);

		//===============================Update Dealer Location===========================================

		// handle userId location mapping.....
		//		LocationFilterRequest req = new LocationFilterRequest();
		//		req.setClientId(clientId);
		//		req.setUserId(userId);
		//		List<DealerLocation> dealerLocList = dealerLocationService.getFilteredDealerLocation(req);
		if (request != null && dealerLocation != null) {

			if (request.getAppointmentLink() != null)
				dealerLocation.setAppointmentLink(request.getAppointmentLink());

			if (request.getLanguageCode() != null)
				dealerLocation.setLanguageCode(request.getLanguageCode());

			if (request.getInstagramAttribute() != null)
				dealerLocation.setInstagramUrl(request.getInstagramAttribute());

			if (request.getFacebookAttribute() != null)
				dealerLocation.setFacebookUrl(request.getFacebookAttribute());

			if (request.getYoutubeAttribute() != null)
				dealerLocation.setYoutubeUrl(request.getYoutubeAttribute());

			if (request.getTwitterAttribute() != null)
				dealerLocation.setTwitterUrl(request.getTwitterAttribute());

			if (request.getWhatsAppAttribute() != null)
				dealerLocation.setWhatsappUrl(request.getWhatsAppAttribute());

			if (request.getLinkedinAttribute() != null)
				dealerLocation.setLinkedinUrl(request.getLinkedinAttribute());

			if (request.getArea() != null)
				dealerLocation.setArea(request.getArea());

			if (request.getCity() != null)
				dealerLocation.setCity(request.getCity());

			if (request.getState() != null)
				dealerLocation.setState(request.getState());

			if (request.getLatitude() != null)
				dealerLocation.setLatitude(request.getLatitude());

			if (request.getLongitude() != null)
				dealerLocation.setLongitude(request.getLongitude());

			if (request.getPincode() != null)
				dealerLocation.setPincode(request.getPincode());

			if (request.getCountry() != null)
				dealerLocation.setCountry(request.getCountry());

			if (request.getAddress() != null)
				dealerLocation.setAddress(request.getAddress());

			if (request.getDescription() != null)
				dealerLocation.setDescription(request.getDescription());

			// Safe join for additional phones
			if (request.getAdditionalPhones() != null && !request.getAdditionalPhones().isEmpty()) {
				dealerLocation.setAdditionalPhones(
						String.join(",", request.getAdditionalPhones())
						);
			}

			// Safe join for labels
			if (request.getLabels() != null && !request.getLabels().isEmpty()) {
				dealerLocation.setCircle(
						String.join(",", request.getLabels())
						);
			}
		}

		if (request.operationHours != null) {

			DealerOperationHours dealerOperationHours = new DealerOperationHours();
			HoursOfOperationRequest operationHours = request.operationHours;
			List<GMBDay> days = operationHours.days;

			for (GMBDay day : days) {
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_MONDAY)){
					dealerOperationHours.setMondayOpenTime(day.openTime);
					dealerOperationHours.setMondayCloseTime(day.closeTime);
				}
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_TUESDAY)){
					dealerOperationHours.setTuesdayOpenTime(day.openTime);
					dealerOperationHours.setTuesdayCloseTime(day.closeTime);
				}
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_WEDNESDAY)){
					dealerOperationHours.setWednesdayOpenTime(day.openTime);
					dealerOperationHours.setWednesdayCloseTime(day.closeTime);
				}
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_THURSDAY)){
					dealerOperationHours.setThursdayOpenTime(day.openTime);
					dealerOperationHours.setThursdayCloseTime(day.closeTime);
				}
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_FRIDAY)){
					dealerOperationHours.setFridayOpenTime(day.openTime);
					dealerOperationHours.setFridayCloseTime(day.closeTime);
				}
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_SATURDAY)){
					dealerOperationHours.setSaturdayOpenTime(day.openTime);
					dealerOperationHours.setSaturdayCloseTime(day.closeTime);
				}
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_SUNDAY)){
					dealerOperationHours.setSundayOpenTime(day.openTime);
					dealerOperationHours.setSundayCloseTime(day.closeTime);
				}
				dealerLocation.setDealerOperationHours(dealerOperationHours);
			}
		}
		dealerLocationRepository.save(dealerLocation);
	}

	//handle exceptions

	public LocationDetailsDto fetchDealerLocationDetailsByDealer(UserDetails userDetailsDto) {
	    String clientId = userDetailsDto.getClientId();
	    String dealerId = userDetailsDto.getDealerId();
	    
	    DealerLocation dealerLocation =
	            dealerLocationRepository.getDealerLocationByDealerIdAndClientId(dealerId, clientId);

	    if (dealerLocation == null) {
	    	  throw new ResourceNotFoundException(
	                  "Dealer location not found for dealerId: " + dealerId +
	                  " and clientId: " + clientId
	          );
	    }
	    
	    String dealerName = dealerLocation.getDealerName();
	  
	    // 1. POPULATE GMB CATEGORY MAP
	    Map<Long, GMBCategory> allGMBCategoryMap = new HashMap<>();
	    try {
	        List<GMBCategory> allGMBCategories = gmbCategoryRepository.findAll();
	        if (!allGMBCategories.isEmpty()) {
	            for (GMBCategory category : allGMBCategories) {
	                // NOTE: Change to category.getCategoryId() if map keys are string-based identifiers
	                allGMBCategoryMap.put(category.getId(), category);
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("Error populating GMB category map: " + e.getMessage());
	    }

	    // 2. POPULATE FACEBOOK CATEGORY MAP
	    Map<Long, FacebookCategory> allFBCategoryMap = new HashMap<>();
	    try {
	        List<FacebookCategory> fbCategories = facebookCategoryRepository.findAll();
	        if (!fbCategories.isEmpty()) {
	            for (FacebookCategory category : fbCategories) {
	                // NOTE: Change to category.getCategoryId() if map keys are string-based identifiers
	                allFBCategoryMap.put(category.getId(), category);
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("Error populating Facebook category map: " + e.getMessage());
	    }

	    // 3. FETCH CORE DATABASE ENTITIES
	    Optional<Client> clientOpt = Optional.empty();
	    try {
	        clientOpt = clientRepository.findByClientId(clientId);
	    } catch (Exception e) {
	        System.err.println("Error fetching Client: " + e.getMessage());
	    }
	    String clientName = clientOpt.map(Client::getClientName).orElse("Unknown Client");
	    String email = clientOpt.map(Client::getEmail).orElse("");

	    /*
	    DealerLocation dealerLocation = null;
	    try {
	        dealerLocation = dealerLocationRepository.getDealerLocationByDealerIdAndClientId(dealerId, clientId);
	        dealerName = dealerLocation.getDealerName();
	        } catch (Exception e) {
	        System.err.println("Error fetching DealerLocation: " + e.getMessage());
	    }

	    // If we can't find the baseline dealer location, the entire mapping fails early
	    if (dealerLocation == null) {
	        System.err.println("Critical Error: DealerLocation record is completely missing for dealer: " + dealerId);
	        return new LocationDetailsDto(clientId, clientId, clientId, null, null, null, null, Collections.emptyMap());
	    }
	     */
	    GMBLocation gmbLocation = null;
	    try {
	        gmbLocation = gmbLocationService.getAllGmbLocationByClientIdAndDealerId(clientId, dealerId);
	    } catch (Exception e) {
	        System.err.println("Error fetching GMBLocation: " + e.getMessage());
	    }

	    FacebookLocation facebookLocation = null;
	    try {
	        facebookLocation = facebookLocationService.getFacebookLocationByClientIdAndDealerId(clientId, dealerId);
	    } catch (Exception e) {
	        System.err.println("Error fetching FacebookLocation: " + e.getMessage());
	    }

	    // --------------------------------------------------------------------------------------------------
	    // 4. BUILD SUB-DTOS (Isolated in independent try-catches so one failure doesn't break the whole app)
	    // --------------------------------------------------------------------------------------------------
	    
	    // --- LOCATION OVERVIEW DTO ---
	    LocationOverviewDto locationOverviewDto = null;
	    try {
	        List<String> labels = new ArrayList<>();
	        String circleRaw = dealerLocation.getCircle();
	        if (circleRaw != null && !circleRaw.isBlank()) {
	            labels = Arrays.stream(circleRaw.split(","))
	                    .map(String::trim)
	                    .toList();
	        }

	        locationOverviewDto = new LocationOverviewDto(
	                clientId, 
	                dealerLocation.getAddress(), 
	                dealerLocation.getAddress2(), 
	                dealerLocation.getAddress3(), 
	                dealerLocation.getArea(), 
	                dealerLocation.getCity(), 
	                dealerLocation.getState(), 
	                dealerLocation.getPincode(), 
	                dealerLocation.getCountry(), 
	                labels, 
	                dealerLocation.getLatitude(), 
	                dealerLocation.getLongitude(), 
	                dealerLocation.getDealerOperationHours(), 
	                dealerLocation.getDescription(),
	                dealerLocation.getGmbPhoneNumber()
	        );
	    } catch (Exception e) {
	        System.err.println("Failed to build LocationOverviewDto: " + e.getMessage());
	    }

	    // --- GMB DETAILS DTO ---
	    GmbDetailsDto gmbDetailsDto = null;
	    try {
	        Long primaryGmbId = dealerLocation.getPrimaryCategoryGMB();
	        GMBCategory gmbCategory = (primaryGmbId != null) ? allGMBCategoryMap.get(primaryGmbId) : null;
	        CategoryDetails gmbPrimaryCategoryDetails = (gmbCategory != null) 
	                ? new CategoryDetails(gmbCategory.getId(), gmbCategory.getDisplayName()) 
	                : null;

	        List<CategoryDetails> gmbAddCategoryList = new ArrayList<>();
	        String gmbAddCategoriesRaw = dealerLocation.getAdditionalCategoriesGMB();
	        if (gmbAddCategoriesRaw != null && !gmbAddCategoriesRaw.isBlank()) {
	            gmbAddCategoryList = Arrays.stream(gmbAddCategoriesRaw.split(","))
	                    .map(String::trim)
	                    .filter(str -> str.matches("\\d+")) 
	                    .map(Long::parseLong)
	                    .map(allGMBCategoryMap::get)
	                    .filter(Objects::nonNull)
	                    .map(cat -> new CategoryDetails(cat.getId(), cat.getDisplayName()))
	                    .toList();
	        }

	        gmbDetailsDto = new GmbDetailsDto(
	                dealerLocation.getDealerName(), 
	                gmbLocation != null ? gmbLocation.getMapUrl() : null, 
	                gmbLocation != null ? gmbLocation.getNewReviewUrl() : null, 
	                gmbLocation != null ? gmbLocation.getWebsiteUrl() : null, 
	                dealerLocation.getGmbPhoneNumber(), 
	                gmbPrimaryCategoryDetails, 
	                gmbAddCategoryList, 
	                dealerLocation.getStatus(), 
	                dealerLocation.getOpenInfoStatus(), 
	                dealerLocation.getLanguageCode(), 
	                dealerLocation.getAppointmentLink(), 
	                dealerLocation.getWhatsappUrl(), 
	                dealerLocation.getFacebookUrl(), 
	                dealerLocation.getTwitterUrl(), 
	                dealerLocation.getInstagramUrl(), 
	                dealerLocation.getLinkedinUrl(), 
	                dealerLocation.getYoutubeUrl()
	        );
	    } catch (Exception e) {
	        System.err.println("Failed to build GmbDetailsDto: " + e.getMessage());
	    }

	    // --- FACEBOOK DETAILS DTO ---
	    FacebookDetailsDto facebookDetailsDto = null;
	    try {
	        Long primaryFbId = dealerLocation.getPrimaryCategoryFacebook();
	        FacebookCategory facebookCategory = (primaryFbId != null) ? allFBCategoryMap.get(primaryFbId) : null;
	        CategoryDetails fbPrimaryCategoryDetails = (facebookCategory != null) 
	                ? new CategoryDetails(facebookCategory.getId(), facebookCategory.getDisplayName()) 
	                : null;

	        List<CategoryDetails> fbAddCategoryList = new ArrayList<>();
	        String fbAddCategoriesRaw = dealerLocation.getAdditionalCategoriesFacebook();
	        if (fbAddCategoriesRaw != null && !fbAddCategoriesRaw.isBlank()) {
	            fbAddCategoryList = Arrays.stream(fbAddCategoriesRaw.split(","))
	                    .map(String::trim)
	                    .filter(str -> str.matches("\\d+"))
	                    .map(Long::parseLong)
	                    .map(allFBCategoryMap::get)
	                    .filter(Objects::nonNull)
	                    .map(cat -> new CategoryDetails(cat.getId(), cat.getDisplayName()))
	                    .toList();
	        }

	        facebookDetailsDto = new FacebookDetailsDto(
	                dealerLocation.getDealerName(), 
	                facebookLocation != null ? facebookLocation.getParentPageId() : null, 
	                dealerLocation.getFacebookPageId(),
	                facebookLocation != null ? facebookLocation.getWebsiteUrl() : null, 
	                facebookLocation != null ? facebookLocation.getFacebookPageUrl() : null, 
	                fbPrimaryCategoryDetails, 
	                fbAddCategoryList, 
	                facebookLocation != null && facebookLocation.isPublished()
	        );
	    } catch (Exception e) {
	        System.err.println("Failed to build FacebookDetailsDto: " + e.getMessage());
	    }

	    // --- CAMPAIGN SETTINGS DTO ---
	    CampaignSettingsDto campaignSettingsDto = null;
	    try {
	        Optional<ClientAccountSetup> clientAccountSetupOpt = clientAccountSetupRepository.findByClientId(clientId);
	        Optional<ClientLocationSetup> clientLocationSetupOpt = clientLocationSetupRepository.findByClientIdAndDealerId(clientId, dealerId);

	        if (clientAccountSetupOpt.isPresent() && clientLocationSetupOpt.isPresent()) {
	            ClientAccountSetup clientAccountSetup = clientAccountSetupOpt.get();
	            ClientLocationSetup clientLocationSetup = clientLocationSetupOpt.get();

	            campaignSettingsDto = new CampaignSettingsDto(
	                    clientAccountSetup.getIndustry(), 
	                    clientAccountSetup.getSubIndustry(), 
	                    clientLocationSetup.getRadius(),
	                    clientLocationSetup.getRadiusUnit(), 
	                    email, 
	                    clientLocationSetup.getClientCampaignPhoneNumber(),
	                    clientLocationSetup.getAdPhoneNumber(), 
	                    clientLocationSetup.getLandingPageUrl(), 
	                    clientAccountSetup.getYoutubeVideoUrl(), 
	                    clientAccountSetup.getPlatform(), 
	                    ""
	            );
	        } else {
	            System.err.println("Skipped Campaign Settings: Setup records missing for client: " + clientId);
	        }
	    } catch (Exception e) {
	        System.err.println("Failed to build CampaignSettingsDto: " + e.getMessage());
	    }

	    // --- MEDIA DTO (dynamic — grouped by "PLATFORM_CATEGORY" key, whatever exists for this dealer, minus superseded) ---
	    // COVER/LOGO are singleton categories (LocationImage.SINGLETON_CATEGORIES) — expose as a single object, not a one-element array.
	    Map<String, Object> mediaMap = new HashMap<>();
	    try {
	        List<LocationImageMap> visibleMedia = locationImageService.getVisibleMediaForDealer(dealerId);
	        Map<String, List<LocationMediaDto>> groupedMedia = visibleMedia.stream()
	                .map(m -> new LocationMediaDto(
	                        m.getImageId(),
	                        m.getLocationImage().getImageUrl(),
	                        m.getLocationImage().getImageCategory(),
	                        m.getLocationImage().getPlatform(),
	                        m.getStatus()))
	                .collect(Collectors.groupingBy(dto -> dto.getPlatform() + "_" + dto.getCategory()));

	        for (Map.Entry<String, List<LocationMediaDto>> entry : groupedMedia.entrySet()) {
	            List<LocationMediaDto> mediaList = entry.getValue();
	            String category = mediaList.get(0).getCategory();
	            if (LocationImage.SINGLETON_CATEGORIES.contains(category)) {
	                mediaMap.put(entry.getKey(), mediaList.get(0));
	            } else {
	                mediaMap.put(entry.getKey(), mediaList);
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("Failed to build media map: " + e.getMessage());
	    }

	    // --- HEALTH SCORE (GBP profile-completeness, 0-100) ---
	    int healthScore = (gmbLocation != null) ? gbpCompletenessScoreCalculator.calculateScore(gmbLocation) : 0;

	    // 5. RETURN COMBINED ROOT DTO
	    return new LocationDetailsDto(
	            dealerName,
	            dealerId,
	            clientName,
	            locationOverviewDto,
	            gmbDetailsDto,
	            facebookDetailsDto,
	            campaignSettingsDto,
	            mediaMap,
	            healthScore
	    );
	}

	public ViewAllLocationsResponse getAllLocations(UserDetails userDetails) {

		String clientId = userDetails.getClientId();

		Pageable pageable = PageRequest.of(userDetails.getPageNo(), 10);

		String searchText = userDetails.getSearchText();

		Page<DealerLocation> dealerLocList;

		Set<String> mappedDealers = getLocationsMappedToUser(userDetails.getClientId(), userDetails.getUserId());

		if(searchText == null || searchText.isEmpty()) {

			dealerLocList = dealerLocationRepository.findByClientIdAndDealerIdIn(clientId, pageable, mappedDealers);

		}else {

			dealerLocList = dealerLocationRepository.searchLocations(clientId, mappedDealers, searchText, pageable);
			//	dealerLocList = dealerLocationRepository.findByClientIdAndDealerNameContainingIgnoreCase(clientId, dealerName, pageable);

		}

		List<ViewAllLocations> allLocationsList = new ArrayList<>();

		if(dealerLocList.hasContent()) {

			for(DealerLocation dealerLoc : dealerLocList) {

				ViewAllLocations allLocations = new ViewAllLocations(dealerLoc.getDealerId(), dealerLoc.getDealerName(), dealerLoc.getArea(),
						dealerLoc.getCity(), dealerLoc.getState());

				allLocationsList.add(allLocations);
			}

		}else {
			//change to logger// also send exception to the Front End
			System.out.println("Dealer Location list is empty for client id - "+clientId);
		}
		return new ViewAllLocationsResponse(allLocationsList, dealerLocList.getTotalPages(), dealerLocList.getTotalElements());

	}

	public DealerList fetchDealerListByClient(String clientId, String userId) {
		LocationFilterRequest req = new LocationFilterRequest();
		req.setClientId(clientId);
		req.setUserId(userId);
		//List<DealerLocation> dealerLocList = dealerLocationService.getFilteredDealerLocation(req);
		List<GMBLocation> gmbLocList = gmbLocationService.getFilteredGMBLocation(req);
		List<FacebookLocation> facebookLocList = facebookLocationService.getFilteredFacebookLocation(req);
		//List<DealerLocation> dealerLocList = dealerLocationRepository.findByClientId(clientId);

		Map<String,String> dealerIdVsName = new HashMap<>();
		List<GMBDealerList> gmbDealerList = new ArrayList<GMBDealerList>();

		if(!gmbLocList.isEmpty()) {

			for(GMBLocation dealerLoc : gmbLocList) {
				GMBDealerList gmbDealer = GMBDealerList.builder()
						.dealerId(dealerLoc.getDealerId())
						.dealerName(dealerLoc.getName())
						.state(dealerLoc.getState())
						.city(dealerLoc.getCity())
						.build();
				gmbDealerList.add(gmbDealer);
				//dealerIdVsName.put(dealerLoc.getDealerId(), dealerLoc.getDealerName());
			}

		}else {
			System.out.println("GMB Location list is empty for client id - "+clientId);
		}

		List<FacebookDealerList> facebookDealerList = new ArrayList<FacebookDealerList>();
		if(!facebookLocList.isEmpty()) {

			for(FacebookLocation dealerLoc : facebookLocList) {
				FacebookDealerList facebookDealer = FacebookDealerList.builder()
						.dealerId(dealerLoc.getDealerId())
						.dealerName(dealerLoc.getName())
						.state(dealerLoc.getState())
						.city(dealerLoc.getCity())
						.build();
				facebookDealerList.add(facebookDealer);
				//dealerIdVsName.put(dealerLoc.getDealerId(), dealerLoc.getDealerName());
			}

		}else {
			System.out.println("Facebook Location list is empty for client id - "+clientId);
		}

		DealerList dealerIdAndName = new DealerList(clientId, gmbDealerList, facebookDealerList);

		return dealerIdAndName;
	}

	public void updateInfo(String clientId) throws SQLException, IOException, FileNotFoundException, java.io.IOException {

		MyBusinessBusinessInformation businessLocation = gmbSessionFactory.getGMBLocationSession(clientId);

		List<GMBInfoQueue> allGMBInfoQueue = gmbInfoQueueService.getAllGMBInfoQueueByClientId(clientId);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		if (allGMBInfoQueue != null) {
			for (GMBInfoQueue gmbInfo : allGMBInfoQueue) {
				if (gmbInfo.getStatus().equalsIgnoreCase(GMBInfoQueue.STATUS_SUBMIT)) {
					GMBLocation gmbLocation = gmbLocationService.getAllGmbLocationByClientIdAndDealerId(clientId, gmbInfo.getDealerId());
					GMBInfoRequest info = gson.fromJson(gmbInfo.getRequestJson(), GMBInfoRequest.class);

					try {
						//	log("Updating Info Id : " + gmbInfo.getId());
						Thread.sleep(100);
						gmbLocation = updateGMBLocationInfo(clientId, businessLocation, gmbLocation, info, null, false);
						if (gmbLocation != null) {
							gmbInfoQueueService.updateStatusById(GMBInfoQueue.STATUS_DEPLOYED,
									gmbInfo.getId());
							//		log("Updated status as deployed for dealer id:: "+gmbLocation.getDealerId());
							// gmbLocationService.deleteGMBInfoQueueById(clientName, gmbInfo.getId());
						}
					} catch (Exception e) {
						e.printStackTrace();
						//	log("Error deploying info - " + gmbInfo.getId() + " : Error Message - " + e.getMessage());
						gmbInfoQueueService.updateStatusById(GMBInfoQueue.STATUS_ERROR, gmbInfo.getId());
					}
				}
			}
		}
	}

	//modularize the code, segregate the attribute update info, also the category service code, try and follow solid principles, each method should have only single responsibility
	public GMBLocation updateGMBLocationInfo(String clientId, MyBusinessBusinessInformation business, GMBLocation gmbLocation,
			GMBInfoRequest infoRequest, Logger logger, boolean testMode) throws SQLException, IOException, java.io.IOException {
		// System.out.println(infoRequest.labels);

		String updateMask = "";
		String attributeMask = "";

		Location execute = null;
		Location location = new Location().setLanguageCode("en");

		if(infoRequest.locationTitle == null && infoRequest.websiteUrl == null && infoRequest.description == null &&
				infoRequest.gmbPrimaryCategory == null && infoRequest.gmbAdditionalCategories == null && 
				infoRequest.operationHours == null && infoRequest.phoneNumber == null && infoRequest.additionalPhones == null 
				&& infoRequest.labels==null && infoRequest.appointmentLink==null && infoRequest.categoryServices==null && 
				infoRequest.instagramAttribute == null && infoRequest.facebookAttribute == null && infoRequest.twitterAttribute == null && infoRequest.youtubeAttribute == null && infoRequest.whatsAppAttribute == null && infoRequest.linkedinAttribute == null) {
			System.out.println("Nothing to update");
			return null;
		} else if (infoRequest.categoryServices != null) {

			if (!StringUtils.isEmpty(infoRequest.categoryServices.categoryId)) {
				String locationId = "";
				if (gmbLocation.getGmbLocationId() != null) {
					String[] split = gmbLocation.getGmbLocationId().split("/");
					locationId = split[2] + "/" + split[3];
				}
				ArrayList<String> freeFormServices = infoRequest.categoryServices.freeFoundServiceItem;
				ArrayList<String> structuredServices = infoRequest.categoryServices.structuredServiceItem;

				String structuredServiceDescription = infoRequest.categoryServices.structuredServiceDescription == null  ? "" : infoRequest.categoryServices.structuredServiceDescription;
				String freeFoundServiceDescription = infoRequest.categoryServices.freeFoundServiceDescription == null  ? "" : infoRequest.categoryServices.freeFoundServiceDescription;

				updateMask += "serviceItems,";
				List<GMBCategoryServices> allGMBCategoryServices = categoryServicesService.getAllGMBCategoryServices();
				Map<String, GMBCategoryServices> collect = allGMBCategoryServices.stream().collect(Collectors
						.toMap(GMBCategoryServices::getDisplayName, e -> e, (existing, replacement) -> existing));
				//GMBCategoryServices gmbCategoryServices = collect.get("categories/" + infoRequest.categoryServices.categoryId);
				List<ServiceItem> serviceItems = new ArrayList<ServiceItem>();
				if (infoRequest.categoryServices.freeFoundServiceItem != null
						&& !infoRequest.categoryServices.freeFoundServiceItem.isEmpty()) {
					for (String freeService : infoRequest.categoryServices.freeFoundServiceItem) {
						FreeFormServiceItem freeFormServiceItem = new FreeFormServiceItem();
						freeFormServiceItem.setCategory(infoRequest.categoryServices.categoryId);
						Label label = new Label();
						label.setDisplayName(freeService);
						if(!freeFoundServiceDescription.isEmpty()) {
							label.setDescription(freeFoundServiceDescription);
						}
						label.setLanguageCode("en");

						freeFormServiceItem.setLabel(label);

						ServiceItem freeServiceItem = new ServiceItem();
						freeServiceItem.setFreeFormServiceItem(freeFormServiceItem);
						serviceItems.add(freeServiceItem);

					}
				}
				if (infoRequest.categoryServices.structuredServiceItem != null
						&& !infoRequest.categoryServices.structuredServiceItem.isEmpty()) {
					for (String structuredService : infoRequest.categoryServices.structuredServiceItem) {
						GMBCategoryServices gmbCategoryServices = collect.get(structuredService);
						if (gmbCategoryServices == null) {
							System.out.println("No mapping found for " + structuredService);
							continue;
						}
						StructuredServiceItem structuredItem = new StructuredServiceItem();
						structuredItem.setServiceTypeId(gmbCategoryServices.getServiceId());
						if (!structuredServiceDescription.isEmpty()) {
							structuredItem.setDescription(structuredServiceDescription);
						}
						ServiceItem structureItem = new ServiceItem();
						structureItem.setStructuredServiceItem(structuredItem);
						serviceItems.add(structureItem);
					}
				}
				location.setServiceItems(serviceItems);

				System.out.println("location : "+location);
				Location loc = business.locations().patch(locationId, location).setUpdateMask(updateMask)
						.setValidateOnly(false).execute();
				System.out.println(loc.getName() + " " + loc.getServiceItems());
			}

		} else if (infoRequest.locationTitle != null || infoRequest.websiteUrl != null
				|| infoRequest.description != null || infoRequest.gmbPrimaryCategory != null
				|| infoRequest.gmbAdditionalCategories != null || infoRequest.operationHours != null
				|| infoRequest.phoneNumber != null || infoRequest.additionalPhones != null || infoRequest.labels != null
				|| infoRequest.appointmentLink != null) {

			if (infoRequest.locationTitle != null) {
				System.out.println("Gmb title :: " + infoRequest.locationTitle);
				location.setTitle(infoRequest.locationTitle);
				updateMask += "title,";
			}
			if (infoRequest.websiteUrl != null) {
				location.setWebsiteUri(infoRequest.websiteUrl);
				updateMask += "websiteUri,";
			}
			if (infoRequest.description != null) {
				Profile profile = new Profile();
				profile.setDescription(infoRequest.description);
				location.setProfile(profile);
				updateMask += "profile,";
			}

			if (infoRequest.phoneNumber != null || infoRequest.additionalPhones != null) {
				PhoneNumbers phoneNumbers = new PhoneNumbers();
				if (infoRequest.phoneNumber != null) {
					phoneNumbers.setPrimaryPhone(infoRequest.phoneNumber);
				}
				if (infoRequest.additionalPhones != null) {
					phoneNumbers.setAdditionalPhones(infoRequest.additionalPhones);
				}
				location.setPhoneNumbers(phoneNumbers);
				updateMask += "phoneNumbers,";
			}

			if (infoRequest.languageCode != null) {
				location.setLanguageCode(infoRequest.languageCode);
				updateMask += "languageCode,";
			}

			Categories categories = new Categories();
			if (infoRequest.gmbPrimaryCategory != null) {
				Category primaryCategory = new Category();
				primaryCategory.setName(infoRequest.gmbPrimaryCategory.getCategoryId());
				primaryCategory.setDisplayName(infoRequest.gmbPrimaryCategory.getDisplayName());

				categories.setPrimaryCategory(primaryCategory);
				location.setCategories(categories);
				updateMask += "categories,";
			}
			if (infoRequest.gmbAdditionalCategories != null) {
				List<Category> additionalCategories = new ArrayList<Category>();
				for (GMBCategory gmbAddCategory : infoRequest.gmbAdditionalCategories) {
					Category addCategory = new Category();
					addCategory.setName(gmbAddCategory.getCategoryId());
					addCategory.setDisplayName(gmbAddCategory.getDisplayName());
					additionalCategories.add(addCategory);
				}
				categories.setAdditionalCategories(additionalCategories);
				location.setCategories(categories);
				// updateMask += "categories,";
			}
			if (infoRequest.labels != null) {
				List<String> labels = new ArrayList<String>();
				for (String label : infoRequest.labels) {
					labels.add(label);
				}
				location.setLabels(labels);
				updateMask += "labels,";
			}
			if (infoRequest.operationHours != null) {
				BusinessHours hours = new BusinessHours();
				List<TimePeriod> timePeriods = new ArrayList<>();

				HoursOfOperationRequest operationHours = infoRequest.operationHours;
				List<GMBDay> days = operationHours.days;

				for (GMBDay day : days) {
					TimePeriod time = new TimePeriod();
					time.setCloseDay(day.closeDay);
					TimeOfDay closeTime = gmbHelper.getTime(day.closeTime);
					time.setCloseTime(closeTime);
					time.setOpenDay(day.openDay);
					TimeOfDay openTime = gmbHelper.getTime(day.openTime);
					time.setOpenTime(openTime);
					timePeriods.add(time);
				}

				hours.setPeriods(timePeriods);
				location.setRegularHours(hours);
				updateMask += "regularHours,";
			}

			String response = "";
			String locationID = "";
			if (gmbLocation.getGmbLocationId() != null) {
				String[] split = gmbLocation.getGmbLocationId().split("/");
				locationID = split[2] + "/" + split[3];
			}
			if (infoRequest.appointmentLink != null) {
				String appointmentLink = infoRequest.appointmentLink;
				if(gmbLocation.getPlaceActionId() == null || gmbLocation.getPlaceActionId().isEmpty()) {
					String placeActionId = createPlaceActionData(locationID, appointmentLink);
					if(!placeActionId.isEmpty()) {
						System.out.println("appointmentlink updated for the dealer id - "+gmbLocation.getDealerId()+" and appointment link : "+appointmentLink);
						gmbLocationService.updatePlaceActionIdAndAppointmentLinkByDealerId(placeActionId, appointmentLink, gmbLocation.getDealerId());
					}

				} else if(!gmbLocation.getPlaceActionId().isEmpty() ){
					String placeActionId = updatePlaceActionData(gmbLocation.getPlaceActionId(), appointmentLink);
					if(!placeActionId.isEmpty()) {
						System.out.println("appointmentlink updated for the dealer id - "+gmbLocation.getDealerId()+" and appointment link : "+appointmentLink);
						gmbLocationService.updatePlaceActionIdAndAppointmentLinkByDealerId(placeActionId, appointmentLink, gmbLocation.getDealerId());
					}
				}

				response = gmbLocation.getGmbLocationId();
			}

			if (updateMask.endsWith(",")) {
				updateMask = updateMask.substring(0, updateMask.length() - 1);
			}

			if (updateMask != null && !"".equalsIgnoreCase(updateMask.trim())) {
				System.out.println("updateMask - " + updateMask);
				if (!testMode) {
					execute = business.locations().patch(locationID, location).setUpdateMask(updateMask)
							.setValidateOnly(false).execute();
					response = execute.getName();
				}
				if (logger != null && testMode == true) {
					logger.info("\t\t\t" + gmbLocation.getDealerId() + "\t\t\t" + GMBLocation.LOCATION_SOURCE_GMB
							+ "\t\t\t" + updateMask + "\t\t\tUpdate");
				}
				gmbLocation = setGMBLocation(execute, gmbLocation);
				return gmbLocation;
			}
		}else if(infoRequest.instagramAttribute != null || infoRequest.facebookAttribute != null || infoRequest.twitterAttribute != null || infoRequest.youtubeAttribute != null || infoRequest.whatsAppAttribute != null || infoRequest.linkedinAttribute != null) {

			String locationID = "";
			Attributes attributes = new Attributes();

			if(gmbLocation.getGmbLocationId() != null) {
				String[] split = gmbLocation.getGmbLocationId().split("/");
				locationID = "locations/"+split[3];
			}

			// Prepare attributes + get final attributeMask
			AttributeResult attributeResult = setAllAttributeList(infoRequest, gmbLocation);
			System.out.println("Prepared attribute list: " + attributeResult.attributeList.size());
			for (Attribute attr : attributeResult.attributeList) {
				System.out.println(" - Attribute: " + (attr != null ? attr.getName() : "null"));
			}
			attributes.setAttributes(attributeResult.attributeList);

			boolean atLeastOneUpdated = false;

			for (Attribute attr : attributeResult.attributeList) {
				Attributes singleAttr = new Attributes();
				singleAttr.setAttributes(Collections.singletonList(attr));

				String singleMask = attr.getName(); // e.g., "attributes/url_instagram"
				System.out.println("Attr:"+attr+ " attr name = "+attr.getName());
				try {
					Attributes result = business.locations()
							.updateAttributes(locationID + "/attributes", singleAttr)
							.setAttributeMask(singleMask)
							.execute();

					if (result.getAttributes() != null && !result.getAttributes().isEmpty()) {
						atLeastOneUpdated = true;
					}
				} catch (GoogleJsonResponseException e) {
					System.out.println("Error: " + e.getStatusCode() + " - " + e.getDetails());
					System.out.print("Error message "+ e.getDetails());
				}
			}

			if (atLeastOneUpdated) {
				gmbLocationService.updateAttributesByDealerId(gmbLocation.getWhatsappUrl(), gmbLocation.getInstagramUrl(), gmbLocation.getFacebookUrl(), gmbLocation.getTwitterUrl(), gmbLocation.getYoutubeUrl(), gmbLocation.getLinkedinUrl(), gmbLocation.getDealerId());
			}

			if (attributeResult.attributeMask.contains("attributes/url_whatsapp")) {
				//	dealerLocationService.updateUrlAttributesByDealerId(clientName, gmbLocation.getWhatsappUrl(), gmbLocation.getDealerId());
				//already updating while storing req --check with nikita
			}

		}
		return gmbLocation;
	}
	public String createPlaceActionData(String locationId, String appointmentLink) throws IOException, FileNotFoundException, java.io.IOException{

		MyBusinessPlaceActions myBusinessPlaceActions = gmbSessionFactory.getPlaceActionBusiness();
		PlaceActionLink placeActionLink = new PlaceActionLink();
		placeActionLink.setUri(appointmentLink);
		placeActionLink.setPlaceActionType(GMBInfoQueue.PLACE_ACTION_TYPE_APPOINTMENT);

		PlaceActionLink execute = myBusinessPlaceActions.locations().placeActionLinks().create(locationId, placeActionLink).execute();
		String placeActionId = execute.getName();
		return placeActionId;
	}

	public String updatePlaceActionData(String placeActionIds, String appointmentLink) throws IOException, java.io.IOException {

		MyBusinessPlaceActions myBusinessPlaceActions = gmbSessionFactory.getPlaceActionBusiness();
		PlaceActionLink placeActionLink = new PlaceActionLink();
		placeActionLink.setUri(appointmentLink);
		placeActionLink.setPlaceActionType(GMBInfoQueue.PLACE_ACTION_TYPE_APPOINTMENT);
		PlaceActionLink execute = myBusinessPlaceActions.locations().placeActionLinks().patch(placeActionIds, placeActionLink).setUpdateMask("uri").execute();
		String placeActionId = execute.getName();

		return placeActionId;
	}

	private GMBLocation setGMBLocation(Location execute, GMBLocation gmbLocation) throws SQLException {

		if (execute != null) {
			if (execute.getTitle() != null) {
				gmbLocation.setName(execute.getTitle());
			}

			if (execute.getWebsiteUri() != null) {
				gmbLocation.setWebsiteUrl(execute.getWebsiteUri());
			}

			if (execute.getProfile() != null && execute.getProfile().getDescription() != null) {
				gmbLocation.setDescription(execute.getProfile().getDescription());
			}

			if (execute.getPhoneNumbers() != null) {
				if (execute.getPhoneNumbers().getPrimaryPhone() != null) {
					gmbLocation.setPhoneNumber(execute.getPhoneNumbers().getPrimaryPhone());
				}
				if (execute.getPhoneNumbers().getAdditionalPhones() != null) {
					String additionalPhones = "";
					for (String phone : execute.getPhoneNumbers().getAdditionalPhones()) {
						additionalPhones += phone + ",";
					}
					if (additionalPhones.endsWith(",")) {
						additionalPhones = additionalPhones.substring(0, additionalPhones.length() - 1);
					}
					gmbLocation.setAdditionalPhones(additionalPhones);
				}
			}

			if (execute.getLanguageCode() != null) {
				gmbLocation.setLanguageCode(execute.getLanguageCode());
			}

			if (execute.getCategories() != null) {
				if (execute.getCategories().getPrimaryCategory() != null) {
					String categoryId = execute.getCategories().getPrimaryCategory().getName() != null
							? execute.getCategories().getPrimaryCategory().getName().split("/")[1]
									: "";
					if (StringUtils.isNotEmpty(categoryId)) {

						GMBCategory gmbCategory = gmbCategoryRepository.getGMBCategoryByCategoryId(categoryId);
						if (gmbCategory != null) {
							gmbLocation.setPrimaryCategory(gmbCategory.getId());
						}
					}
				}
				if (execute.getCategories().getAdditionalCategories() != null) {
					String additionalCategories = "";
					for (Category category : execute.getCategories().getAdditionalCategories()) {
						String categoryId = category != null ? category.getName().split("/")[1] : "";
						if (StringUtils.isNotEmpty(categoryId)) {
							GMBCategory gmbCategory = gmbCategoryRepository.getGMBCategoryByCategoryId(categoryId);
							if (gmbCategory != null) {
								additionalCategories += gmbCategory.getId() + ",";
							}
						}
					}
					if (additionalCategories.endsWith(",")) {
						additionalCategories = additionalCategories.substring(0, additionalCategories.length() - 1);
					}
					gmbLocation.setAdditionalCategories(additionalCategories);
				}
			}

			if (execute.getLabels() != null) {
				String labels = "";
				for (String label : execute.getLabels()) {
					labels += label + "/n/r";
				}
				gmbLocation.setLabels(labels);
			}

			if (execute.getRegularHours() != null) {
				BusinessHours hours = execute.getRegularHours();
				List<TimePeriod> periods = hours.getPeriods();
				String openTimeSun = "";
				String openTimeMon = "";
				String openTimeTue = "";
				String openTimeWed = "";
				String openTimeThurs = "";
				String openTimeFri = "";
				String openTimeSat = "";

				String closeTimeSun = "";
				String closeTimeMon = "";
				String closeTimeTue = "";
				String closeTimeWed = "";
				String closeTimeThurs = "";
				String closeTimeFri = "";
				String closeTimeSat = "";
				for (TimePeriod period : periods) {
					if (period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_MONDAY)) {
						TimeOfDay openTime = period.getOpenTime();
						TimeOfDay closeTime = period.getCloseTime();

						openTimeMon = openTime != null ? openTime.toString() : "";
						closeTimeMon = closeTime != null ? closeTime.toString() : "";
					}
					if (period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_TUESDAY)) {
						TimeOfDay openTime = period.getOpenTime();
						TimeOfDay closeTime = period.getCloseTime();

						openTimeTue = openTime != null ? openTime.toString() : "";
						closeTimeTue = closeTime != null ? closeTime.toString() : "";
					}
					if (period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_WEDNESDAY)) {
						TimeOfDay openTime = period.getOpenTime();
						TimeOfDay closeTime = period.getCloseTime();

						openTimeWed = openTime != null ? openTime.toString() : "";
						closeTimeWed = closeTime != null ? closeTime.toString() : "";
					}
					if (period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_THURSDAY)) {
						TimeOfDay openTime = period.getOpenTime();
						TimeOfDay closeTime = period.getCloseTime();

						openTimeThurs = openTime != null ? openTime.toString() : "";
						closeTimeThurs = closeTime != null ? closeTime.toString() : "";
					}
					if (period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_FRIDAY)) {
						TimeOfDay openTime = period.getOpenTime();
						TimeOfDay closeTime = period.getCloseTime();

						openTimeFri = openTime != null ? openTime.toString() : "";
						closeTimeFri = closeTime != null ? closeTime.toString() : "";
					}
					if (period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_SATURDAY)) {
						TimeOfDay openTime = period.getOpenTime();
						TimeOfDay closeTime = period.getCloseTime();

						openTimeSat = openTime != null ? openTime.toString() : "";
						closeTimeSat = closeTime != null ? closeTime.toString() : "";
					}
					if (period.getOpenDay().equalsIgnoreCase(GMBOperationHours.WORKING_SUNDAY)) {
						TimeOfDay openTime = period.getOpenTime();
						TimeOfDay closeTime = period.getCloseTime();

						openTimeSun = openTime != null ? openTime.toString() : "";
						closeTimeSun = closeTime != null ? closeTime.toString() : "";
					}

				}
				GMBOperationHours gmbOperationHours = gmbLocation.getGmbOperationHours();
				gmbOperationHours.setMondayOpenTime(openTimeMon);
				gmbOperationHours.setMondayCloseTime(closeTimeMon);
				gmbOperationHours.setTuesdayOpenTime(openTimeTue);
				gmbOperationHours.setTuesdayCloseTime(closeTimeTue);
				gmbOperationHours.setWednesdayOpenTime(openTimeWed);
				gmbOperationHours.setWednesdayCloseTime(closeTimeWed);
				gmbOperationHours.setThursdayOpenTime(openTimeThurs);
				gmbOperationHours.setThursdayCloseTime(closeTimeThurs);
				gmbOperationHours.setFridayOpenTime(openTimeFri);
				gmbOperationHours.setFridayCloseTime(closeTimeFri);
				gmbOperationHours.setSaturdayOpenTime(openTimeSat);
				gmbOperationHours.setSaturdayCloseTime(closeTimeSat);
				gmbOperationHours.setSundayOpenTime(openTimeSun);
				gmbOperationHours.setSundayCloseTime(closeTimeSun);

				gmbLocation.setGmbOperationHours(gmbOperationHours);
			}

			return gmbLocation;
		}
		return null;
	}

	public AttributeResult setAllAttributeList(GMBInfoRequest infoRequest, GMBLocation location) {
		List<Attribute> attributeList = new ArrayList<>();
		StringBuilder attributeMask = new StringBuilder();

		if (infoRequest.instagramAttribute != null) {
			Attribute attr = createSocialAttribute("attributes/url_instagram", infoRequest.instagramAttribute);
			attributeList.add(attr);
			attributeMask.append("attributes/url_instagram,");
			location.setInstagramUrl(infoRequest.instagramAttribute);
		}

		if (infoRequest.facebookAttribute != null) {
			Attribute attr = createSocialAttribute("attributes/url_facebook", infoRequest.facebookAttribute);
			attributeList.add(attr);
			attributeMask.append("attributes/url_facebook,");
			location.setFacebookUrl(infoRequest.facebookAttribute);
		}

		if (infoRequest.youtubeAttribute != null) {
			Attribute attr = createSocialAttribute("attributes/url_youtube", infoRequest.youtubeAttribute);
			attributeList.add(attr);
			attributeMask.append("attributes/url_youtube,");
			location.setYoutubeUrl(infoRequest.youtubeAttribute);
		}

		if (infoRequest.twitterAttribute != null) {
			Attribute attr = createSocialAttribute("attributes/url_twitter", infoRequest.twitterAttribute);
			attributeList.add(attr);
			attributeMask.append("attributes/url_twitter,");
			location.setTwitterUrl(infoRequest.twitterAttribute);
		}

		if (infoRequest.whatsAppAttribute != null) {
			Attribute attr = createSocialAttribute("attributes/url_whatsapp", infoRequest.whatsAppAttribute);
			attributeList.add(attr);
			attributeMask.append("attributes/url_whatsapp,");
			location.setWhatsappUrl(infoRequest.whatsAppAttribute); // Corrected
		}

		if (infoRequest.linkedinAttribute != null) {
			Attribute attr = createSocialAttribute("attributes/url_linkedin", infoRequest.linkedinAttribute);
			attributeList.add(attr);
			attributeMask.append("attributes/url_linkedin,");
			location.setLinkedinUrl(infoRequest.linkedinAttribute);
		}

		// Remove trailing comma
		if (attributeMask.length() > 0) {
			attributeMask.setLength(attributeMask.length() - 1);
		}

		return new AttributeResult(attributeList, attributeMask.toString());
	}
	private Attribute createSocialAttribute(String name, String url) {
		Attribute attribute = new Attribute();
		attribute.setName(name);
		attribute.setValueType("URL");

		UriAttributeValue uriValue = new UriAttributeValue();
		uriValue.setUri(url);

		attribute.setUriValues(Collections.singletonList(uriValue));
		return attribute;
	}

	public AccountVerifyResponseDto isGroupExists(String groupName) throws Exception {

		AccountVerifyResponseDto responseDto = new AccountVerifyResponseDto();
		MyBusinessAccountManagement business = gmbSessionFactory.getGMBAccountSession();
		String nextPageToken = null;
		do {
			MyBusinessAccountManagement.Accounts.List request =
					business.accounts().list();
			if(nextPageToken != null) {
				request.setPageToken(nextPageToken);
			}
			ListAccountsResponse response = request.execute();

			List<Account> accounts = response.getAccounts();

			if(accounts != null) {

				for(Account account : accounts) {

					String apiGroupName = account.getAccountName(); // or getName()
					String accountNumber = account.getAccountNumber();

					if(apiGroupName != null &&
							apiGroupName.equalsIgnoreCase(groupName)) {
						responseDto.setMessage("Account Group Name exists");
						responseDto.setExist(true);
						responseDto.setAccountName(apiGroupName);
						responseDto.setAccountNumber(accountNumber);
						return responseDto; // group found
					}
				}
			}
			nextPageToken = response.getNextPageToken();
		} while(nextPageToken != null);
		responseDto.setMessage("Account Group Name does'nt exists");
		responseDto.setExist(false);
		return responseDto; // group not found after checking all pages
	}
	public List<CategoryDto> fetchCategories(String source) {

	    List<CategoryDto> categoryDtos = new ArrayList<>();

	    if (GMBLocation.LOCATION_SOURCE_GMB.equalsIgnoreCase(source)) {

	        List<GMBCategory> categories = gmbCategoryRepository.findAll();

	        categoryDtos = categories.stream()
	                .map(category -> new CategoryDto(
	                        category.getId(),
	                        category.getDisplayName()))
	                .toList();

	    } else if (FacebookLocation.LOCATION_SOURCE_FACEBOOK.equalsIgnoreCase(source)) {

	        List<FacebookCategory> categories = facebookCategoryRepository.findAll();

	        categoryDtos = categories.stream()
	                .map(category -> new CategoryDto(
	                        category.getId(),
	                        category.getDisplayName()))
	                .toList();
	    }

	    return categoryDtos;
	}

	@Transactional
	public void updateCampaignSettings(String dealerId, String clientId,
	        CampaignSettingsBody request) {

	    if (request == null) {
	        throw new IllegalArgumentException("Campaign Settings Request is null");
	    }

	    ClientAccountSetup clientAccountSetup = clientAccountSetupRepository
	            .findByClientId(clientId)
	            .orElseThrow(() -> new RuntimeException(
	                    "ClientAccountSetup not found for client id: " + clientId));

	    ClientLocationSetup clientLocationSetup = clientLocationSetupRepository
	            .findByClientIdAndDealerId(clientId, dealerId)
	            .orElseThrow(() -> new RuntimeException(
	                    "ClientLocationSetup not found for client id: "
	                            + clientId + " and dealer id: " + dealerId));

	    if (StringUtils.isNotBlank(request.getYoutubeUrl())) {
	        clientAccountSetup.setYoutubeVideoUrl(request.getYoutubeUrl());
	    }

	    if (request.getRadius() != null) {
	        clientLocationSetup.setRadius(request.getRadius());
	    }

	    if (StringUtils.isNotBlank(request.getRadiusUnit())) {
	        clientLocationSetup.setRadiusUnit(request.getRadiusUnit());
	    }

	    if (StringUtils.isNotBlank(request.getCampaignPhoneNumber())) {
	        clientLocationSetup.setClientCampaignPhoneNumber(
	                request.getCampaignPhoneNumber());
	    }

	    if (StringUtils.isNotBlank(request.getCallAdsPhoneNumber())) {
	        clientLocationSetup.setAdPhoneNumber(
	                request.getCallAdsPhoneNumber());
	    }

	    if (StringUtils.isNotBlank(request.getLandingPageUrl())) {
	        clientLocationSetup.setLandingPageUrl(
	                request.getLandingPageUrl());
	    }

	    clientAccountSetupRepository.save(clientAccountSetup);
	    clientLocationSetupRepository.save(clientLocationSetup);
	}

	@Transactional
	public void updateGMBDetails(String dealerId, String clientId, GMBDetailsBody request) {
		// Merger with tickets for GMB
		if (request == null) {
		    throw new IllegalArgumentException("GMB Details Request is null");
		}

		DealerLocation dealerLocation = dealerLocationRepository.getDealerLocationByDealerIdAndClientId(dealerId, clientId);
		if (dealerLocation == null) {
		    throw new RuntimeException(
		        "Dealer Location not found for dealerId: " + dealerId +
		        " and clientId: " + clientId
		    );
		}
		
		GMBInfoRequest gmbInfoRequest = new GMBInfoRequest();
	
		if (request.getGmbPrimaryCategory() != null) {
			Long gmbPrimaryCategoryId = request.getGmbPrimaryCategory();
			GMBCategory gmbCategory = gmbCategoryRepository.findById(gmbPrimaryCategoryId)
					.orElseThrow(() -> new RuntimeException("GMB category not found: " +gmbPrimaryCategoryId));

			gmbInfoRequest.gmbPrimaryCategory = gmbCategory;
			dealerLocation.setPrimaryCategoryGMB(gmbPrimaryCategoryId);
		}

		if(request.getGmbAdditionalCategories() != null && !request.getGmbAdditionalCategories().isEmpty()) {
			List<Long> gmbAdditionCategoryIds = request.getGmbAdditionalCategories();
			List<GMBCategory> gmbAdditionalCategories = new ArrayList<>();
			for(Long addCategoryId :gmbAdditionCategoryIds) {
				GMBCategory gmbCat = gmbCategoryRepository.findById(addCategoryId)
						.orElseThrow(() -> new RuntimeException("GMB category not found: " + addCategoryId));

				gmbAdditionalCategories.add(gmbCat);
			}
			gmbInfoRequest.gmbAdditionalCategories = gmbAdditionalCategories;

			String gmbAdditionCategories = gmbAdditionCategoryIds.stream()
					.map(cat -> String.valueOf(cat))
					.collect(Collectors.joining(","));
			dealerLocation.setAdditionalCategoriesGMB(gmbAdditionCategories);
		}
		if (StringUtils.isNotBlank(request.getAppointmentLink())) {
			gmbInfoRequest.appointmentLink = request.getAppointmentLink();
			dealerLocation.setAppointmentLink(request.getAppointmentLink());
		}
		if (StringUtils.isNotBlank(request.getLanguageCode())) {
			gmbInfoRequest.languageCode = request.getLanguageCode();
			dealerLocation.setLanguageCode(request.getLanguageCode());
		}
		if (request.getCategoryServices() != null) {
			gmbInfoRequest.categoryServices = request.getCategoryServices();
		}
		if (StringUtils.isNotBlank(request.getInstagramAttribute())) {
			gmbInfoRequest.instagramAttribute = request.getInstagramAttribute();
			dealerLocation.setInstagramUrl(request.getInstagramAttribute());
		}
		if (StringUtils.isNotBlank(request.getFacebookAttribute())) {
			gmbInfoRequest.facebookAttribute = request.getFacebookAttribute();
			dealerLocation.setFacebookUrl(request.getFacebookAttribute());
		}
		if (StringUtils.isNotBlank(request.getYoutubeAttribute())) {
			gmbInfoRequest.youtubeAttribute = request.getYoutubeAttribute();
			dealerLocation.setYoutubeUrl(request.getYoutubeAttribute());
		}
		if (StringUtils.isNotBlank(request.getTwitterAttribute())) {
			gmbInfoRequest.twitterAttribute = request.getTwitterAttribute();
			dealerLocation.setTwitterUrl(request.getTwitterAttribute());
		}
		if (StringUtils.isNotBlank(request.getWhatsAppAttribute())) {
			gmbInfoRequest.whatsAppAttribute = request.getWhatsAppAttribute();
			dealerLocation.setWhatsappUrl(request.getWhatsAppAttribute());
		}
		if (StringUtils.isNotBlank(request.getLinkedinAttribute())) {
			gmbInfoRequest.linkedinAttribute = request.getLinkedinAttribute();
			dealerLocation.setLinkedinUrl(request.getLinkedinAttribute());
		}
		dealerLocationService.saveLocation(dealerLocation);
		//save gmbInfoRequest in GMBInfoQueue table
		gmbInfoQueueService.saveGMBInfoRequest(dealerId, gmbInfoRequest);
	}
	
	@Transactional
	public void updatefacebookDetails(String dealerId, String clientId, FacebookDetailsBody request) {
	
		if (request == null) {
		    throw new IllegalArgumentException("Facebook Details Request is null");
		}
		DealerLocation dealerLocation = dealerLocationRepository.getDealerLocationByDealerIdAndClientId(dealerId, clientId);

		if (dealerLocation == null) {
		    throw new RuntimeException(
		        "Dealer Location not found for dealerId: " + dealerId +
		        " and clientId: " + clientId
		    );
		}
		FacebookInfoRequest fbInfoRequest = new FacebookInfoRequest();
		
		if (request.getFbPrimaryCategory() != null) {

			Long fbPrimaryCategoryId = request.getFbPrimaryCategory();

			FacebookCategory fbPrimaryCategory = facebookCategoryRepository.findById(fbPrimaryCategoryId)
					.orElseThrow(() -> new RuntimeException(
							"Facebook category not found: " + fbPrimaryCategoryId));

			fbInfoRequest.fbPrimaryCategory = fbPrimaryCategory;
			dealerLocation.setPrimaryCategoryFacebook(fbPrimaryCategoryId);
		}
		
		if(request.getFbAdditionalCategories() != null && !request.getFbAdditionalCategories().isEmpty()) {
			List<Long> fbAdditionCategoryIds = request.getFbAdditionalCategories();
			List<FacebookCategory> fbAdditionalCategories = new ArrayList<>();
			for (Long addCategoryId : fbAdditionCategoryIds) {
				FacebookCategory fbCat = facebookCategoryRepository.findById(addCategoryId)
						.orElseThrow(() -> new RuntimeException(
								"Facebook category not found: " + addCategoryId));

				fbAdditionalCategories.add(fbCat);
			}
			fbInfoRequest.fbAdditionalCategories = fbAdditionalCategories;

			String fbAdditionCategories = fbAdditionalCategories.stream()
					.map(cat -> String.valueOf(cat.getId()))
					.collect(Collectors.joining(","));

			dealerLocation.setAdditionalCategoriesFacebook(fbAdditionCategories);
		}
	
		if (StringUtils.isNotBlank(request.getStoreLocationDescriptor())) {
			fbInfoRequest.StoreLocationDescriptor = request.getStoreLocationDescriptor();
			dealerLocation.setNameWithLocationDesc(request.getStoreLocationDescriptor());
		}
		if (StringUtils.isNotBlank(request.getArea())) {
			fbInfoRequest.area = request.getArea();
			dealerLocation.setArea(request.getArea());
		}
		if (StringUtils.isNotBlank(request.getCity())) {
			fbInfoRequest.city = request.getCity();
			dealerLocation.setCity(request.getCity());
		}
		if (StringUtils.isNotBlank(request.getState())) {
			fbInfoRequest.state = request.getState();
			dealerLocation.setState(request.getState());
		}
		if (StringUtils.isNotBlank(request.getPincode())) {
			fbInfoRequest.pincode = request.getPincode();
			dealerLocation.setPincode(request.getPincode());
		}
		if (StringUtils.isNotBlank(request.getCountry())) {
			fbInfoRequest.country = request.getCountry();
			dealerLocation.setCountry(request.getCountry());
		}
		String address1 = dealerLocation.getAddress1();
		String address2 = dealerLocation.getAddress2();
		String address3 = dealerLocation.getAddress3();
		
		if (request.getAddress1() != null && !request.getAddress1().isEmpty()) {
			address1 = request.getAddress1();
			dealerLocation.setAddress1(address1);		}
	
		if (request.getAddress2() != null && !request.getAddress2().isEmpty()) {
			address2 = request.getAddress2();
			dealerLocation.setAddress2(address2);
		}
	
		if (request.getAddress3() != null && !request.getAddress3().isEmpty()) {
			address3 = request.getAddress3();
			dealerLocation.setAddress3(address3);	
		}
		
		String address = address1 + address2 + address3;
		fbInfoRequest.address = address;
		dealerLocation.setAddress(address);
		
		if (StringUtils.isNotBlank(request.getLatitude())) {
			fbInfoRequest.latitude = request.getLatitude();
			dealerLocation.setLatitude(request.getLatitude());
		}
		if (StringUtils.isNotBlank(request.getLongitude())) {
			fbInfoRequest.longitude = request.getLongitude();
			dealerLocation.setLongitude(request.getLongitude());
		}
		
		dealerLocationService.saveLocation(dealerLocation);
		facebookInfoQueueService.saveFacebookInfoRequest(dealerId, fbInfoRequest);
	}
	
	@Transactional
	public void updateLocationOverviewDetails(String dealerId, String clientId,
			LocationOverviewBody request) {
		
		DealerLocation dealerLocation = dealerLocationRepository.getDealerLocationByDealerIdAndClientId(dealerId, clientId);
	
		if(request == null) {
			throw new SecurityException("Request is Null");
		}
		if(dealerLocation == null) {
			
			throw new SecurityException("Dealer Location Not Found");
		}
		
		FacebookInfoRequest fbInfoRequest = new FacebookInfoRequest();
		GMBInfoRequest gmbInfoRequest = new GMBInfoRequest();
	
		if (StringUtils.isNotBlank(request.getLocationTitle())) {
			gmbInfoRequest.locationTitle = request.getLocationTitle();
			dealerLocation.setGmbTitle(request.getLocationTitle());
		}
		if (StringUtils.isNotBlank(request.getWebsiteUrl())) {
			gmbInfoRequest.websiteUrl = request.getWebsiteUrl();
		}
		if (StringUtils.isNotBlank(request.getDescription())) {
			gmbInfoRequest.description = request.getDescription();
		}
		if (request.getLabels() != null && !request.getLabels().isEmpty()) {
			gmbInfoRequest.labels = request.getLabels();
		}
		if (request.getOperationHours() != null) {
			gmbInfoRequest.operationHours = request.getOperationHours();
		}
		if (StringUtils.isNotBlank(request.getPhoneNumber())) {
			gmbInfoRequest.phoneNumber = request.getPhoneNumber();
			dealerLocation.setGmbPhoneNumber(request.getPhoneNumber());
		}
		if (request.getAdditionalPhones() != null && !request.getAdditionalPhones().isEmpty()) {
			gmbInfoRequest.additionalPhones = request.getAdditionalPhones();
		}

		//save gmbInfoRequest in GMBInfoQueue table
		gmbInfoQueueService.saveGMBInfoRequest(dealerId, gmbInfoRequest);

	//=========================== Facebook ======================================
	

		if (StringUtils.isNotBlank(request.getLocationTitle())) {
			fbInfoRequest.locationTitle = request.getLocationTitle();
		}
		if (StringUtils.isNotBlank(request.getWebsiteUrl())) {
			fbInfoRequest.websiteUrl = request.getWebsiteUrl();
		}
		if (StringUtils.isNotBlank(request.getDescription())) {
			fbInfoRequest.description = request.getDescription();
		}
		if (request.getLabels() != null && !request.getLabels().isEmpty()) {
			fbInfoRequest.labels = request.getLabels();
		}
		if (request.getAdditionalPhones() != null && !request.getAdditionalPhones().isEmpty()) {
			fbInfoRequest.additionalPhones = request.getAdditionalPhones();
		}

		if (StringUtils.isNotBlank(request.getPhoneNumber())) {
			fbInfoRequest.phoneNumber = request.getPhoneNumber();
			dealerLocation.setMetaPhoneNumber(request.getPhoneNumber());
		}
		if (request.getOperationHours() != null) {
			fbInfoRequest.operationHours = request.getOperationHours();
		}
		facebookInfoQueueService.saveFacebookInfoRequest(dealerId, fbInfoRequest);
	//=================================================================================
		if (StringUtils.isNotBlank(request.getDescription())) {
			dealerLocation.setDescription(request.getDescription());
		}
		
		if (request.getAdditionalPhones() != null) {
		    dealerLocation.setAdditionalPhones(
		        String.join(",", request.getAdditionalPhones())
		    );
		}

		if (request.getLabels() != null) {
		    dealerLocation.setCircle(
		        String.join(",", request.getLabels())
		    );
		}
	//================== Dealer Location ==============================================
		if (request.operationHours != null) {

			DealerOperationHours dealerOperationHours = dealerLocation.getDealerOperationHours();
			HoursOfOperationRequest operationHours = request.operationHours;
			List<GMBDay> days = operationHours.days;

			for (GMBDay day : days) {
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_MONDAY)){
					dealerOperationHours.setMondayOpenTime(day.openTime);
					dealerOperationHours.setMondayCloseTime(day.closeTime);
				}
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_TUESDAY)){
					dealerOperationHours.setTuesdayOpenTime(day.openTime);
					dealerOperationHours.setTuesdayCloseTime(day.closeTime);
				}
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_WEDNESDAY)){
					dealerOperationHours.setWednesdayOpenTime(day.openTime);
					dealerOperationHours.setWednesdayCloseTime(day.closeTime);
				}
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_THURSDAY)){
					dealerOperationHours.setThursdayOpenTime(day.openTime);
					dealerOperationHours.setThursdayCloseTime(day.closeTime);
				}
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_FRIDAY)){
					dealerOperationHours.setFridayOpenTime(day.openTime);
					dealerOperationHours.setFridayCloseTime(day.closeTime);
				}
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_SATURDAY)){
					dealerOperationHours.setSaturdayOpenTime(day.openTime);
					dealerOperationHours.setSaturdayCloseTime(day.closeTime);
				}
				if (day.openDay.equalsIgnoreCase(GMBOperationHours.WORKING_SUNDAY)){
					dealerOperationHours.setSundayOpenTime(day.openTime);
					dealerOperationHours.setSundayCloseTime(day.closeTime);
				}
			}
			dealerLocation.setDealerOperationHours(dealerOperationHours);
		}

		dealerLocationRepository.save(dealerLocation);
	}
}
