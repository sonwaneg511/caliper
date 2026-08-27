package com.caliper.location.service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.dto.response.FilteredDealerLocationResponse;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.entity.DealerOperationHours;
import com.caliper.location.facebook.entity.FacebookLocation;
import com.caliper.location.gmb.entity.GMBOperationHours;
import com.caliper.location.repository.DealerLocationRepository;
import com.caliper.location.specification.DealerLocationSepcification;
import com.caliper.usermanagement.entity.UserClientLocMapping;
import com.caliper.usermanagement.repository.UserClientLocMappingRepository;
import com.caliper.utils.exception.customException.ResourceNotFoundException;

@Service
public class DealerLocationService {

	@Autowired
	private DealerLocationRepository dealerLocationRepository;

	@Autowired
	private UserClientLocMappingRepository userClientLocMappingRepository;

	public DealerLocation saveLocation(DealerLocation dealerLocation) {
		return dealerLocationRepository.save(dealerLocation);
	}

	public List<DealerLocation> getFilteredDealerLocation(LocationFilterRequest req) {

		List<UserClientLocMapping> userClientLocMapping =
				userClientLocMappingRepository.findByUserIdAndclientId(
						req.getUserId(), req.getClientId());

		Set<String> dealerIds = userClientLocMapping.stream()
				.map(UserClientLocMapping::getDealerId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		if (dealerIds.isEmpty()) {
			throw new ResourceNotFoundException("Dealers are not mapped for given user id : " + req.getUserId()+ " and client id : " + req.getClientId());
		}

		List<String> validDealerIds;

		// Request dealerId is null or empty. allow all user dealers
		if (req.getDealerId() == null || req.getDealerId().isEmpty()) {
			validDealerIds = new ArrayList<>(dealerIds);
		}
		// Request dealerId present 
		//---------------------code review:handle exception of dealers not present------------------------------
		else {
			validDealerIds = req.getDealerId().stream()
					.filter(dealerIds::contains)
					.toList();

			if (validDealerIds.isEmpty()) {
				return Collections.emptyList();
			}
		}
		//------------------------------------------code review------------------------------------------------------
		List<DealerLocation> locations = dealerLocationRepository.findAll(DealerLocationSepcification.filterLocations(req, validDealerIds));
		return locations;
	}


	public List<DealerLocation> getFilteredDealerLocationByUserId(String clientId, String userId) {

		List<UserClientLocMapping> userClientLocMapping = userClientLocMappingRepository.findByUserIdAndclientId(userId, clientId);
		Set<String> dealerIds = userClientLocMapping.stream().map(UserClientLocMapping::getDealerId).filter(Objects::nonNull).collect(Collectors.toSet());
		if (dealerIds.isEmpty()) {
			throw new ResourceNotFoundException(
					"Dealers are not mapped for given user id : " + userId + " and client id : " + clientId);
		}
		List<DealerLocation> locations = findByDealerIdInAndClientId(dealerIds, clientId);
		return locations;
	}

	public DealerLocation getDealerLocationByDealerId(String dealerId, String clientId) {
		return dealerLocationRepository.getDealerLocationByDealerIdAndClientId(dealerId, clientId);
	}

	public List<DealerLocation> getAllDealerLocationsByClientId(String clientId) {
		return dealerLocationRepository.findByClientId(clientId);
	}

	public FilteredDealerLocationResponse getDropDownFilteredDealerLocation(LocationFilterRequest req) {
		//VALID DEALER IDS FOR USER + CLIENT
		List<UserClientLocMapping> mappings = userClientLocMappingRepository.findByUserIdAndclientId(req.getUserId(), req.getClientId());

		Set<String> dealerIdSet = mappings.stream()
				.map(UserClientLocMapping::getDealerId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		if (dealerIdSet.isEmpty()) {
			throw new ResourceNotFoundException("Dealers are not mapped for userId : " + req.getUserId() + " and clientId : " + req.getClientId());
		}

		List<String> validDealerIds = new ArrayList<>(dealerIdSet);

		//-------------------------------------------COUNTRY DROPDOWN user + client only (NO country filter)-----------------------------------------------
		List<DealerLocation> countryLocations = dealerLocationRepository.findAll(DealerLocationSepcification.filterDropdownLocations(req, validDealerIds, false, false, false));

		List<String> countries = countryLocations.stream()
				.map(DealerLocation::getCountry)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

		//----------------------------------------------------STATE DROPDOWN country only-------------------------------------------------------------
		List<DealerLocation> stateLocations = dealerLocationRepository.findAll(DealerLocationSepcification.filterDropdownLocations(req, validDealerIds, true, false, false));

		List<String> states = stateLocations.stream()
				.map(DealerLocation::getState)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

		//---------------------------------------------------CITY DROPDOWN country + state------------------------------------------------------------
		List<DealerLocation> cityLocations = dealerLocationRepository.findAll(DealerLocationSepcification.filterDropdownLocations(req, validDealerIds, true, true, false));

		List<String> cities = cityLocations.stream()
				.map(DealerLocation::getCity)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

		//------------------------------------------------DEALER DROPDOWN country + state + city-------------------------------------------------------
		List<DealerLocation> dealerLocations = dealerLocationRepository.findAll(DealerLocationSepcification.filterDropdownLocations(req, validDealerIds, true, true, true));

		//		List<String> dealers = dealerLocations.stream()
		//				.map(DealerLocation::getDealerId)
		//				.filter(Objects::nonNull)
		//				.distinct()
		//				.toList();

		Map<String, String> dealerIdVsName = dealerLocations.stream()
				.filter(l -> l.getDealerId() != null)
				.collect(Collectors.toMap(
						DealerLocation::getDealerId,
						DealerLocation::getDealerName,
						(existing, replacement) -> existing, // avoid overwrite
						LinkedHashMap::new
						));
		//------------------------------------------------------------RESPONSE-------------------------------------------------------------------------
		FilteredDealerLocationResponse response = new FilteredDealerLocationResponse();

		response.setCountry(countries);
		response.setState(states);
		response.setCity(cities);
		response.setDealerList(dealerIdVsName);

		return response;
	}


	public Long fetchLocationsCount(String clientId, Set<String>dealerIds) {
		return dealerLocationRepository.countByClientIdAndDealerIdIn(clientId, dealerIds);
	}


	public List<DealerLocation> findByDealerIdIn(Set<String> mappedDealers) {
		return dealerLocationRepository.findByDealerIdIn(mappedDealers);
	}

	public List<DealerLocation> findByDealerIdInAndClientId(Set<String> dealerIds, String clientId){
		return dealerLocationRepository.findByDealerIdInAndClientId(dealerIds, clientId);
	}

	//working code
	//	public FilteredDealerLocationResponse getDropDownFilteredDealerLocation(LocationFilterRequest req) {
	//		List<DealerLocation> locations = getFilteredDealerLocation(req);
	//		FilteredDealerLocationResponse filteredDealerLocationResponse = new FilteredDealerLocationResponse();
	//
	//		List<String> countries = locations.stream()
	//				.map(DealerLocation::getCountry)
	//				.filter(Objects::nonNull)
	//				.distinct()
	//				.collect(Collectors.toList());
	//
	//		List<String> cities = locations.stream()
	//				.map(DealerLocation::getCity)
	//				.filter(Objects::nonNull)
	//				.distinct()
	//				.collect(Collectors.toList());
	//
	//		List<String> states = locations.stream()
	//				.map(DealerLocation::getState)
	//				.filter(Objects::nonNull)
	//				.distinct()
	//				.collect(Collectors.toList());
	//
	//		List<String> dealerIds = locations.stream()
	//				.map(DealerLocation::getDealerId)
	//				.filter(Objects::nonNull)
	//				.distinct()
	//				.collect(Collectors.toList());
	//
	//		filteredDealerLocationResponse.setCountry(countries);
	//		filteredDealerLocationResponse.setState(states);
	//		filteredDealerLocationResponse.setCity(cities);
	//		filteredDealerLocationResponse.setDealerId(dealerIds);
	//		return filteredDealerLocationResponse;
	//	}
	
	public void insertDealerOperationHours(
            String clientId,
            String dealerId,
            GMBOperationHours inputHours, DealerLocation dealerLocation) {

		DealerOperationHours hours = new DealerOperationHours();
	    hours.setClientId(clientId);
	    hours.setDealerId(dealerId);

	    hours.setMondayOpenTime(inputHours.getMondayOpenTime());
	    hours.setMondayCloseTime(inputHours.getMondayCloseTime());
	    hours.setTuesdayOpenTime(inputHours.getTuesdayOpenTime());
	    hours.setTuesdayCloseTime(inputHours.getTuesdayCloseTime());
	    hours.setWednesdayOpenTime(inputHours.getWednesdayOpenTime());
	    hours.setWednesdayCloseTime(inputHours.getWednesdayCloseTime());
	    hours.setThursdayOpenTime(inputHours.getThursdayOpenTime());
	    hours.setThursdayCloseTime(inputHours.getThursdayCloseTime());
	    hours.setFridayOpenTime(inputHours.getFridayOpenTime());
	    hours.setFridayCloseTime(inputHours.getFridayCloseTime());
	    hours.setSaturdayOpenTime(inputHours.getSaturdayOpenTime());
	    hours.setSaturdayCloseTime(inputHours.getSaturdayCloseTime());
	    hours.setSundayOpenTime(inputHours.getSundayOpenTime());
	    hours.setSundayCloseTime(inputHours.getSundayCloseTime());

	    dealerLocation.setDealerOperationHours(hours);

	    dealerLocationRepository.save(dealerLocation);
    }

	public void insertFacebookDetailsIntoDealerLocation(String clientId, FacebookLocation fbPage) {

		DealerLocation dealerLocation = dealerLocationRepository
			    .findByClientIdAndDealerId(clientId, fbPage.getDealerId())
			    .orElseGet(DealerLocation::new);
	// don't create new loc
		dealerLocation.setDealerId(fbPage.getDealerId());
		dealerLocation.setClientId(fbPage.getClientId());
		dealerLocation.setNameWithLocationDesc(fbPage.getNameWithLocationDesc());
		dealerLocation.setPrimaryCategoryFacebook(fbPage.getCategory());
		dealerLocation.setAdditionalCategoriesFacebook(fbPage.getSubCategory());
		dealerLocation.setFacebookEnabled(true);
		dealerLocation.setFacebookPageId(fbPage.getFacebookPageId());
		dealerLocation.setFacebookUrl(fbPage.getFacebookPageUrl());
	}
}