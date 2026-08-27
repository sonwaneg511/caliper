package com.caliper.location.facebook.service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.restfb.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.restfb.Connection;
import com.restfb.types.Page;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import com.restfb.types.Location;
import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.facebook.entity.FacebookCategory;
import com.caliper.location.facebook.entity.FacebookLocation;
import com.caliper.location.facebook.entity.FacebookOperationHours;
import com.caliper.location.facebook.entity.FacebookPage;
import com.caliper.location.facebook.repository.FacebookLocationRepository;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.service.GMBLocationService;
import com.caliper.location.service.DealerLocationService;
import com.caliper.usermanagement.entity.UserClientLocMapping;
import com.caliper.usermanagement.repository.UserClientLocMappingRepository;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.Category;
import com.restfb.Version;

@Slf4j
@Service
public class FacebookLocationService {

	@Autowired
	private FacebookLocationRepository facebookLocationRepository;
	
	@Autowired
	private UserClientLocMappingRepository userClientLocMappingRepository;

	@Autowired
	private FacebookCategoryService facebookCategoryService;

	@Autowired
	private FacebookOperationHoursService facebookOperationHoursService;
	
	@Autowired private DealerLocationService dealerLocationService;

	@Autowired
	private GMBLocationService gmbLocationService;

	public FacebookLocation getFacebookLocationByClientIdAndLocationId(String clientId, String facebookPageId){
		return facebookLocationRepository.getFacebookLocationByClientIdAndFacebookPageId(clientId, facebookPageId);
	}

	public FacebookLocation getFacebookLocationByClientIdAndDealerId(String clientId, String dealerId) {
		return facebookLocationRepository.getFacebookLocationByClientIdAndDealerId(clientId, dealerId);
	}

	public List<FacebookLocation>getAllFacebookLocationsByClientId(String clientId){
		return facebookLocationRepository.findByClientId(clientId);
	}

	public List<FacebookLocation> getLocations(String clientId, FacebookPage facebookAccount, Map<String, GMBLocation> gmbLocationMap) throws Exception {
		boolean getNextPage = true;
		String pageToken = "";

		List<FacebookCategory> fbCategories = facebookCategoryService.getAllFbCategory();
		Map<String,Long> categoryMap = new HashMap<String,Long>();
		for(FacebookCategory category : fbCategories) {
			categoryMap.put(category.getDisplayName(), category.getId());
		}
		FacebookClient fbClient = new DefaultFacebookClient(facebookAccount.getPageAccessToken(), Version.LATEST);
		String facebookPageId = String.valueOf(facebookAccount.getFacebookPageId());
		List<FacebookLocation> fbPages = new ArrayList<FacebookLocation>();
		System.out.println("Parent Page id::"+facebookPageId);
		while(getNextPage) {
			for (int i = 0; i < 4; i++) {
				try {
					Thread.sleep(2000);
					Connection<Page> locations = fetchLocations(pageToken, fbClient, facebookPageId);
					if(locations != null) {
						System.out.println("Page Size::"+locations.getTotalCount());
						for(Page loc : locations.getData()) {
							System.out.println("fb.com/"+loc.getId());
							try {
								FacebookLocation newFacebookPage = getFacebookLocation(clientId, loc, facebookPageId, categoryMap, gmbLocationMap);
								fbPages.add(newFacebookPage);
							}catch(Exception ex) {
								System.out.println("Exception while fetching location details::"+ex);
								ex.printStackTrace();
							}

						}
					}

					pageToken = locations.getAfterCursor();
					if(pageToken == null) {
						getNextPage = false;
					}
					break;
				} catch (Exception ex) {
					// Do Nothing. Try Again.
					if(i == 3) {
						getNextPage = false;
					}
					ex.printStackTrace();
					System.out.println("Error fetching location. Will sleep for 5 secs.");
					Thread.sleep(5000);
				}
			}
		}

		return fbPages;
	}

	private Connection<Page> fetchLocations(String pageToken, FacebookClient fbClient, String pageId) {
		Connection<Page> locations = null;
		if(pageToken != null) {
			locations = fbClient.fetchConnection(pageId+"/locations", Page.class, Parameter.with("fields", "access_token,location,name,store_location_descriptor,store_number,phone,emails,link,website,description,category,is_published,hours,parent_page,category_list"), Parameter.with("limit", "500"), Parameter.with("after", pageToken));
		} else {
			locations = fbClient.fetchConnection(pageId+"/locations", Page.class, Parameter.with("fields", "access_token,location,name,store_location_descriptor,store_number,phone,emails,link,website,description,category,is_published,hours,parent_page,category_list"), Parameter.with("limit", "500"));
		}
		return locations;
	}

	private FacebookLocation getFacebookLocation(String clientName, Page childPage, String parentId,
			Map<String, Long> categoryMap,
			Map<String, GMBLocation> gmbLocationMap) {

		// Determine dealer ID
		String dealerId = childPage.getStoreCode() != null ? childPage.getStoreCode() : childPage.getStoreNumber();
		String parentPageId = (childPage.getParentPage() != null) ? childPage.getParentPage().getId() : parentId;
		String pageId = childPage.getId();
		String name = childPage.getName();
		String storeLocDesc = childPage.getStoreLocationDescriptor();

		// Fetch GMB location info safely
		GMBLocation gmbLocation = gmbLocationMap.get(dealerId);

		// Handle page location
		Location pageLocationDetails = childPage.getLocation();
		String area = "";
		String city = "";
		String state = "";
		String pincode = "";
		String latitude = "";
		String longitude = "";
		String country = "";
		String address = "";

		if (pageLocationDetails != null) {
			area = (pageLocationDetails.getStreet() != null) ? pageLocationDetails.getStreet()
					: (gmbLocation != null ? gmbLocation.getArea() : "");
			city = (pageLocationDetails.getCity() != null) ? pageLocationDetails.getCity() : "";
			state = (pageLocationDetails.getState() != null) ? pageLocationDetails.getState()
					: (gmbLocation != null ? gmbLocation.getState() : "");
			pincode = (pageLocationDetails.getZip() != null) ? pageLocationDetails.getZip() : "";
			latitude = (pageLocationDetails.getLatitude() != null) ? String.valueOf(pageLocationDetails.getLatitude()) : "";
			longitude = (pageLocationDetails.getLongitude() != null) ? String.valueOf(pageLocationDetails.getLongitude()) : "";
			country = (pageLocationDetails.getCountry() != null) ? pageLocationDetails.getCountry() : "";
			address = (area.isEmpty() ? "" : area) +
					(city.isEmpty() ? "" : ", " + city) +
					(country.isEmpty() ? "" : ", " + country);
		}

		// Other page info
		String phone = (childPage.getPhone() != null) ? childPage.getPhone() : "";
		String email = (childPage.getEmails() != null) ? childPage.getEmails().toString() : "";
		String websiteUrl = (childPage.getWebsite() != null) ? childPage.getWebsite() : "";
		String pageLink = (childPage.getLink() != null) ? childPage.getLink() : "";
		String description = (childPage.getDescription() != null) ? childPage.getDescription() : "";

		// Handle categories safely
		String subCategory = "";
		if (childPage.getCategoryList() != null) {
			for (Category category : childPage.getCategoryList()) {
				Long id = (categoryMap.get(category.getName()) != null) ? categoryMap.get(category.getName()) : 0L;
				subCategory += id + ",";
			}
			if (subCategory.endsWith(",")) {
				subCategory = subCategory.substring(0, subCategory.length() - 1);
			}
		}

		Long categoryId = 0L;
		if (childPage.getCategory() != null) {
			categoryId = (categoryMap.get(childPage.getCategory()) != null) ? categoryMap.get(childPage.getCategory()) : 0L;
		}

		// Labels
		String labels = "";
		if (childPage.getLabels() != null && !childPage.getLabels().isEmpty()) {
			labels = childPage.getLabels().toString();
		} else if (gmbLocation != null) {
			labels = gmbLocation.getLabels() != null ? gmbLocation.getLabels() : "";
		}

		boolean isPublished = (childPage.getIsPublished() != null) ? childPage.getIsPublished() : false;
		String accessToken = (childPage.getAccessToken() != null) ? childPage.getAccessToken() : "";

		// Build FacebookLocation entity
		FacebookLocation newFacebookPage = new FacebookLocation(
				pageId, clientName, parentPageId, dealerId, name, storeLocDesc,
				address, city, state, pincode, latitude, longitude, country,
				phone, email, pageLink, websiteUrl, description, categoryId, subCategory,
				labels, isPublished, accessToken, null, "", new Date(), dealerId, null
				);

		// Set operation hours safely
		FacebookOperationHours facebookOperationHours = facebookOperationHoursService.getFacebookOperationHours(childPage);
		newFacebookPage.setFacebookOperationHours(facebookOperationHours);

		return newFacebookPage;
	}

	@Async("clientTaskExecutor")
	public void processFacebookFetchLocations(String clientId, boolean fetchLocations, FacebookPage facebookPageAccount,
			Map<String, GMBLocation> gmbLocationDealerIdVsDbLocationMap,
			Map<String, FacebookLocation> facebookPageIdVsDbLocationMap) throws Exception {

		List<FacebookLocation> facebookAllLocation = new ArrayList<FacebookLocation>();

		log.info("API Call");
		List<FacebookLocation> facebookLocationConsole = getLocations(clientId, facebookPageAccount, gmbLocationDealerIdVsDbLocationMap);
		facebookAllLocation.addAll(facebookLocationConsole);

		for (FacebookLocation fbPage : facebookAllLocation) {

			log.info("Processing page - "+fbPage.getFacebookPageId());

			try {
				boolean containsLocationId = facebookPageIdVsDbLocationMap.containsKey(fbPage.getFacebookPageId());

				if (!containsLocationId) {

					log.info("Inserting new location in table -"+fbPage.getFacebookPageId());

					insertFacebookLocationWithHours(clientId, fbPage);
					dealerLocationService.insertFacebookDetailsIntoDealerLocation(clientId, fbPage);

				} else {

					log.info("Updating existing location -"+fbPage.getFacebookPageId());

					FacebookLocation existingLocation = facebookPageIdVsDbLocationMap.get(fbPage.getFacebookPageId());

					updateFacebookLocationWithHours(clientId, fbPage, existingLocation);
				}
			}catch (Exception e) {
				log.error("Error processing FacebookLocation with ID {} for client {}: {}",
						fbPage.getFacebookPageId(), clientId, e.getMessage(), e);
				continue;
			}

		}
		List<FacebookLocation> fbLocationDBNew = getAllFacebookLocationsByClientId(clientId);
		/*	if (insertBQ) {
			facebookLocationService.deleteFacebookLocationBQ(bigQuery, clientName);
			facebookLocationService.insertFacebookLocationBQ(bigQuery, clientName, fbLocationDBNew);
		}*/

	}

	/**
	 * Blocking counterpart to {@link #processFacebookFetchLocations}, for callers (e.g. the OAuth
	 * callback) that need the fetch to complete before proceeding. Builds the same lookup maps
	 * that {@link com.caliper.task.FacebookPageDeploymentTask} builds per account, then runs the
	 * same fetch/process loop synchronously on the calling thread.
	 */
	public void fetchAndProcessLocationsSync(String clientId, FacebookPage facebookPageAccount) throws Exception {

		Map<String, FacebookLocation> facebookPageIdVsDbLocationMap = new HashMap<String, FacebookLocation>();
		for (FacebookLocation fbPagesDb : getAllFacebookLocationsByClientId(clientId)) {
			facebookPageIdVsDbLocationMap.put(fbPagesDb.getFacebookPageId(), fbPagesDb);
		}

		Map<String, GMBLocation> gmbLocationDealerIdVsDbLocationMap = new HashMap<String, GMBLocation>();
		for (GMBLocation gmbLocation : gmbLocationService.getAllGmbLocationByClientId(clientId)) {
			gmbLocationDealerIdVsDbLocationMap.put(gmbLocation.getDealerId(), gmbLocation);
		}

		List<FacebookLocation> facebookAllLocation = new ArrayList<FacebookLocation>();

		log.info("API Call");
		List<FacebookLocation> facebookLocationConsole = getLocations(clientId, facebookPageAccount, gmbLocationDealerIdVsDbLocationMap);
		facebookAllLocation.addAll(facebookLocationConsole);

		for (FacebookLocation fbPage : facebookAllLocation) {

			log.info("Processing page - "+fbPage.getFacebookPageId());

			try {
				boolean containsLocationId = facebookPageIdVsDbLocationMap.containsKey(fbPage.getFacebookPageId());

				if (!containsLocationId) {

					log.info("Inserting new location in table -"+fbPage.getFacebookPageId());

					insertFacebookLocationWithHours(clientId, fbPage);
					dealerLocationService.insertFacebookDetailsIntoDealerLocation(clientId, fbPage);

				} else {

					log.info("Updating existing location -"+fbPage.getFacebookPageId());

					FacebookLocation existingLocation = facebookPageIdVsDbLocationMap.get(fbPage.getFacebookPageId());

					updateFacebookLocationWithHours(clientId, fbPage, existingLocation);
				}
			}catch (Exception e) {
				log.error("Error processing FacebookLocation with ID {} for client {}: {}",
						fbPage.getFacebookPageId(), clientId, e.getMessage(), e);
				continue;
			}

		}
	}

	public FacebookLocation updateFacebookLocationWithHours(String clientId, FacebookLocation fbPage, FacebookLocation existingLocation) {

		// Update main fields
		existingLocation.setClientId(clientId);
		existingLocation.setParentPageId(fbPage.getParentPageId());
		existingLocation.setName(fbPage.getName());
		existingLocation.setNameWithLocationDesc(fbPage.getNameWithLocationDesc());
		existingLocation.setAddress(fbPage.getAddress());
		existingLocation.setCity(fbPage.getCity());
		existingLocation.setState(fbPage.getState());
		existingLocation.setPincode(fbPage.getPincode());
		existingLocation.setLatitude(fbPage.getLatitude());
		existingLocation.setLongitude(fbPage.getLongitude());
		existingLocation.setCountryCode(fbPage.getCountryCode());
		existingLocation.setPhoneNumber(fbPage.getPhoneNumber());
		existingLocation.setEmail(fbPage.getEmail());
		existingLocation.setFacebookPageUrl(fbPage.getFacebookPageUrl());
		existingLocation.setWebsiteUrl(fbPage.getWebsiteUrl());
		existingLocation.setDescription(fbPage.getDescription());
		existingLocation.setCategory(fbPage.getCategory());
		existingLocation.setSubCategory(fbPage.getSubCategory());
		existingLocation.setLabels(fbPage.getLabels());
		existingLocation.setPublished(fbPage.isPublished());
		existingLocation.setAccessToken(fbPage.getAccessToken());
		existingLocation.setLastInserted(new Date());

		// Update or insert operation hours
		FacebookOperationHours newHours = fbPage.getFacebookOperationHours();
		updateOrInsertOperationHours(existingLocation, newHours);

		facebookLocationRepository.save(existingLocation);

		return existingLocation;
	}

	public void updateOrInsertOperationHours(FacebookLocation fbLocation, FacebookOperationHours newHoursDto) {

	    if (fbLocation == null || newHoursDto == null) return;

	    FacebookOperationHours existingHours = fbLocation.getFacebookOperationHours();

	    if (existingHours != null) {
	        // Update fields
	        existingHours.setMondayOpenTime(newHoursDto.getMondayOpenTime());
	        existingHours.setMondayCloseTime(newHoursDto.getMondayCloseTime());
	        existingHours.setTuesdayOpenTime(newHoursDto.getTuesdayOpenTime());
	        existingHours.setTuesdayCloseTime(newHoursDto.getTuesdayCloseTime());
	        existingHours.setWednesdayOpenTime(newHoursDto.getWednesdayOpenTime());
	        existingHours.setWednesdayCloseTime(newHoursDto.getWednesdayCloseTime());
	        existingHours.setThursdayOpenTime(newHoursDto.getThursdayOpenTime());
	        existingHours.setThursdayCloseTime(newHoursDto.getThursdayCloseTime());
	        existingHours.setFridayOpenTime(newHoursDto.getFridayOpenTime());
	        existingHours.setFridayCloseTime(newHoursDto.getFridayCloseTime());
	        existingHours.setSaturdayOpenTime(newHoursDto.getSaturdayOpenTime());
	        existingHours.setSaturdayCloseTime(newHoursDto.getSaturdayCloseTime());
	        existingHours.setSundayOpenTime(newHoursDto.getSundayOpenTime());
	        existingHours.setSundayCloseTime(newHoursDto.getSundayCloseTime());

	        // No separate save needed; cascade handles it when saving parent
	    } else {
	        FacebookOperationHours opHours = new FacebookOperationHours();
	        opHours.setMondayOpenTime(newHoursDto.getMondayOpenTime());
	        opHours.setMondayCloseTime(newHoursDto.getMondayCloseTime());
	        opHours.setTuesdayOpenTime(newHoursDto.getTuesdayOpenTime());
	        opHours.setTuesdayCloseTime(newHoursDto.getTuesdayCloseTime());
	        opHours.setWednesdayOpenTime(newHoursDto.getWednesdayOpenTime());
	        opHours.setWednesdayCloseTime(newHoursDto.getWednesdayCloseTime());
	        opHours.setThursdayOpenTime(newHoursDto.getThursdayOpenTime());
	        opHours.setThursdayCloseTime(newHoursDto.getThursdayCloseTime());
	        opHours.setFridayOpenTime(newHoursDto.getFridayOpenTime());
	        opHours.setFridayCloseTime(newHoursDto.getFridayCloseTime());
	        opHours.setSaturdayOpenTime(newHoursDto.getSaturdayOpenTime());
	        opHours.setSaturdayCloseTime(newHoursDto.getSaturdayCloseTime());
	        opHours.setSundayOpenTime(newHoursDto.getSundayOpenTime());
	        opHours.setSundayCloseTime(newHoursDto.getSundayCloseTime());

	        opHours.setFacebookLocation(fbLocation);
	        fbLocation.setFacebookOperationHours(opHours);
	    }

	    // Save parent only; cascade will save/update OperationHours
	    facebookLocationRepository.save(fbLocation);
	}

	public void insertFacebookLocationWithHours(String clientId, FacebookLocation fbPage) {

	    FacebookLocation fbLocation = FacebookLocation.builder()
	            .facebookPageId(fbPage.getFacebookPageId())
	            .clientId(clientId)
	            .parentPageId(fbPage.getParentPageId())
	            .dealerId(fbPage.getDealerId())
	            .name(fbPage.getName())
	            .nameWithLocationDesc(fbPage.getNameWithLocationDesc())
	            .address(fbPage.getAddress())
	            .city(fbPage.getCity())
	            .state(fbPage.getState())
	            .pincode(fbPage.getPincode())
	            .latitude(fbPage.getLatitude())
	            .longitude(fbPage.getLongitude())
	            .countryCode(fbPage.getCountryCode())
	            .phoneNumber(fbPage.getPhoneNumber())
	            .email(fbPage.getEmail())
	            .facebookPageUrl(fbPage.getFacebookPageUrl())
	            .websiteUrl(fbPage.getWebsiteUrl())
	            .description(fbPage.getDescription())
	            .category(fbPage.getCategory())
	            .subCategory(fbPage.getSubCategory())
	            .labels(fbPage.getLabels())
	            .isPublished(fbPage.isPublished())
	            .accessToken(fbPage.getAccessToken())
	            .lastInserted(new Date())
	            .build();

	    // Create a new OperationHours entity instead of using DTO directly
	    FacebookOperationHours opHoursDto = fbPage.getFacebookOperationHours();
	    if (opHoursDto != null) {
	        FacebookOperationHours opHours = new FacebookOperationHours();
	        opHours.setClientId(clientId);
	        opHours.setDealerId(fbPage.getDealerId());
	        opHours.setMondayOpenTime(opHoursDto.getMondayOpenTime());
	        opHours.setMondayCloseTime(opHoursDto.getMondayCloseTime());
	        opHours.setTuesdayOpenTime(opHoursDto.getTuesdayOpenTime());
	        opHours.setTuesdayCloseTime(opHoursDto.getTuesdayCloseTime());
	        opHours.setWednesdayOpenTime(opHoursDto.getWednesdayOpenTime());
	        opHours.setWednesdayCloseTime(opHoursDto.getWednesdayCloseTime());
	        opHours.setThursdayOpenTime(opHoursDto.getThursdayOpenTime());
	        opHours.setThursdayCloseTime(opHoursDto.getThursdayCloseTime());
	        opHours.setFridayOpenTime(opHoursDto.getFridayOpenTime());
	        opHours.setFridayCloseTime(opHoursDto.getFridayCloseTime());
	        opHours.setSaturdayOpenTime(opHoursDto.getSaturdayOpenTime());
	        opHours.setSaturdayCloseTime(opHoursDto.getSaturdayCloseTime());
	        opHours.setSundayOpenTime(opHoursDto.getSundayOpenTime());
	        opHours.setSundayCloseTime(opHoursDto.getSundayCloseTime());

	        opHours.setFacebookLocation(fbLocation);
	        fbLocation.setFacebookOperationHours(opHours);
	    }
	    
	    

	    facebookLocationRepository.save(fbLocation); // cascade inserts OperationHours
	}



	public Long fetchLocationsCount(String clientId, Set<String> dealerIds) {

		return facebookLocationRepository.countByClientIdAndDealerIdIn(clientId, dealerIds);
	}
	
	public List<FacebookLocation> getFilteredFacebookLocation(LocationFilterRequest req) {

		List<UserClientLocMapping> userClientLocMapping =
				userClientLocMappingRepository.findByUserIdAndclientId(
						req.getUserId(), req.getClientId());

		Set<String> dealerIds = userClientLocMapping.stream()
				.map(UserClientLocMapping::getDealerId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		if (dealerIds.isEmpty()) {
			throw new ResourceNotFoundException(
					"Dealers are not mapped for given user id : " + req.getUserId()+ " and client id : " + req.getClientId());
		}

		List<String> validDealerIds;

		// Request dealerId is null or empty. allow all user dealers
		if (req.getDealerId() == null || req.getDealerId().isEmpty()) {
			validDealerIds = new ArrayList<>(dealerIds);
		}
		// Request dealerId present 
		else {
			validDealerIds = req.getDealerId().stream()
					.filter(dealerIds::contains)
					.toList();

			if (validDealerIds.isEmpty()) {
				return Collections.emptyList();
			}
		}

		List<FacebookLocation> locations = facebookLocationRepository.findByClientIdAndDealerIdIn(req.getClientId(), validDealerIds);
		return locations;
	}
}