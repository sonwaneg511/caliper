package com.caliper.location.facebook.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.facebook.dto.FacebookInfoRequest;
import com.caliper.location.facebook.entity.FacebookCategory;
import com.caliper.location.facebook.entity.FacebookInfoQueue;
import com.caliper.location.facebook.entity.FacebookLocation;
import com.caliper.location.facebook.entity.FacebookOperationHours;
import com.caliper.location.facebook.repository.FacebookInfoQueueRepository;
import com.caliper.location.gmb.dto.HoursOfOperationRequest.GMBDay;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.FacebookType;
import com.restfb.types.Location;
import com.restfb.types.Page;

@Service
public class FacebookInfoQueueService {

	@Autowired
	private FacebookInfoQueueRepository facebookInfoQueueRepository;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private FacebookCategoryService facebookCategoryService;
	
	@Autowired
	private FacebookOperationHoursService facebookOperationHoursService;

	public List<FacebookInfoQueue>findAllFacebookInfoQueueByClientId(String clientId){
		
		return facebookInfoQueueRepository.findAllFacebookInfoQueueByClientId(clientId);

	}
	
	public FacebookInfoQueue saveFacebookInfoRequest(String dealerId, FacebookInfoRequest facebookInfoRequest) {
		try {

			String json = objectMapper.writeValueAsString(facebookInfoRequest);

			FacebookInfoQueue queueEntry = new FacebookInfoQueue();
			queueEntry.setDealerId(dealerId);
			queueEntry.setRequestJson(json);
			queueEntry.setStatus(FacebookInfoQueue.STATUS_SUBMIT);

			return facebookInfoQueueRepository.save(queueEntry);

		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize FacebookInfoRequest", e);
		} catch (Exception e) {
			throw new RuntimeException("Failed to save FacebookInfoQueue", e);
		}
	}
	
	public void updateFacebookLocationInfoStatus(String status, Long id) {
		facebookInfoQueueRepository.updateFacebookInfoQueueStatusById(status, id);
	}
	
	//Modularize the code
	public FacebookLocation updateFacebookLocationInfo(String clientId, FacebookLocation fbLocation, FacebookInfoRequest infoRequest, Logger logger, boolean testMode)
			throws SQLException, IOException {
		if(infoRequest.locationTitle == null && infoRequest.websiteUrl == null && infoRequest.description == null &&
				infoRequest.fbPrimaryCategory == null && infoRequest.fbAdditionalCategories == null && 
				infoRequest.operationHours == null && infoRequest.phoneNumber == null) {
			System.out.println("Nothing to update");
			return null;
		}
		FacebookClient fbClient = new DefaultFacebookClient(fbLocation.getAccessToken(), Version.LATEST);
		Page page;
		try {
			page = fbClient.fetchObject(fbLocation.getFacebookPageId(), Page.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch Facebook page " + fbLocation.getFacebookPageId() + ": " + e.getMessage(), e);
		}
		System.out.println("Page id::"+page.getId());
		String description = fbLocation.getDescription() == null ? "" : fbLocation.getDescription();
		String storeLocationDesc = fbLocation.getNameWithLocationDesc() == null ? "" : fbLocation.getNameWithLocationDesc();
		String website = fbLocation.getWebsiteUrl() == null ? "" : fbLocation.getWebsiteUrl();
		String phone = fbLocation.getPhoneNumber() == null ? "" : fbLocation.getPhoneNumber();
		FacebookCategory category = facebookCategoryService.getFacebookCategoryById(fbLocation.getCategory());
		
		List<String> facebookSubCategories = Arrays.asList(fbLocation.getSubCategory().replaceAll("\\[", "").replaceAll("\\]", ""));
		Optional<FacebookOperationHours> hours = facebookOperationHoursService.getFacebookOperationHoursByPageId(clientId, fbLocation.getFacebookPageId());
		Map<String,String> hoursMap = new HashMap<String,String>();
		List<String> categories = new ArrayList<String>();
		String address = fbLocation.getAddress() == null ? "" : fbLocation.getAddress();
		String city = fbLocation.getCity() == null ? "" : fbLocation.getCity();
		String state = fbLocation.getState() == null ? "" : fbLocation.getState();
		String latitude = fbLocation.getLatitude() == null ? "" : fbLocation.getLatitude();
		String longitude = fbLocation.getLongitude() == null ? "" : fbLocation.getLongitude();
		String pincode = fbLocation.getPincode() == null ? "" : fbLocation.getPincode();
		String country = fbLocation.getCountryCode() == null ? "" : fbLocation.getCountryCode();
		
		String updateMask = "";
		
		if (infoRequest.locationTitle != null) {
			storeLocationDesc = infoRequest.locationTitle;
			updateMask += "Store Location Desc,";
		}
		
		if (infoRequest.websiteUrl != null) {
			website = infoRequest.websiteUrl;
			updateMask += "Website,";
		}
		
		if (infoRequest.description != null) {
			description = infoRequest.description;
			updateMask += "Description,";
		}
		
		if (infoRequest.fbPrimaryCategory != null) {
		    category = infoRequest.fbPrimaryCategory;
		    updateMask += "Category,";
		}

		categories.add(category.getCategoryId());

		if (infoRequest.fbAdditionalCategories != null) {
			List<FacebookCategory> additionalCategories = infoRequest.fbAdditionalCategories;
			for(FacebookCategory additionalCategory : additionalCategories) {
				categories.add(additionalCategory.getCategoryId());
			}
		} else {
			categories.addAll(facebookSubCategories);
		}
		
		if (infoRequest.address != null) {
			address = infoRequest.address;
			updateMask += "Address,";
		}
		
		if (infoRequest.city != null) {
			city = infoRequest.city;
			updateMask += "City,";
		}
		
		if (infoRequest.state != null) {
			state = infoRequest.state;
			updateMask += "State,";
		}
		
		if (infoRequest.latitude != null) {
			latitude = infoRequest.latitude;
			updateMask += "Latitude,";
		}
		
		if (infoRequest.longitude != null) {
			longitude = infoRequest.longitude;
			updateMask += "Longitude,";
		}
		
		if (infoRequest.pincode != null) {
			pincode = infoRequest.pincode;
			updateMask += "Pincode,";
		}
		
		Location location = new Location();
		
		location.setCity(city);
		location.setCountry(country);
		location.setLatitude(StringUtils.isNotEmpty(latitude) ? Double.valueOf(latitude) : null);
		location.setLongitude(StringUtils.isNotEmpty(longitude) ? Double.valueOf(longitude) : null);
		//location.setRegion(state);
		location.setState(state);
		location.setStreet(address);
		location.setZip(pincode);
		
		if(infoRequest.operationHours != null) {
			List<GMBDay> days = infoRequest.operationHours.days;
			for(GMBDay day : days) {
				String openDay = day.openDay;
				String openTime = day.openTime;
				String closeDay = day.closeDay;
				String closeTime = day.closeTime;
				
				
				if(openDay.equals(FacebookOperationHours.WORKING_MONDAY)) {
					hoursMap.put("mon_1_open", openTime);
				}
				if(closeDay.equals(FacebookOperationHours.WORKING_MONDAY)) {
					hoursMap.put("mon_1_close", closeTime);
				}
				if(openDay.equals(FacebookOperationHours.WORKING_TUESDAY)) {
					hoursMap.put("tue_1_open", openTime);
				}
				if(closeDay.equals(FacebookOperationHours.WORKING_TUESDAY)) {
					hoursMap.put("tue_1_close", closeTime);
				}
				if(openDay.equals(FacebookOperationHours.WORKING_WEDNESDAY)) {
					hoursMap.put("wed_1_open", openTime);
				}
				if(closeDay.equals(FacebookOperationHours.WORKING_WEDNESDAY)) {
					hoursMap.put("wed_1_close", closeTime);
				}
				if(openDay.equals(FacebookOperationHours.WORKING_THURSDAY)) {
					hoursMap.put("thu_1_open", openTime);
				}
				if(closeDay.equals(FacebookOperationHours.WORKING_THURSDAY)) {
					hoursMap.put("thu_1_close", closeTime);
				}
				if(openDay.equals(FacebookOperationHours.WORKING_FRIDAY)) {
					hoursMap.put("fri_1_open", openTime);
				}
				if(closeDay.equals(FacebookOperationHours.WORKING_FRIDAY)) {
					hoursMap.put("fri_1_close", closeTime);
				}
				if(openDay.equals(FacebookOperationHours.WORKING_SATURDAY)) {
					hoursMap.put("sat_1_open", openTime);
				}
				if(closeDay.equals(FacebookOperationHours.WORKING_SATURDAY)) {
					hoursMap.put("sat_1_close", closeTime);
				}
				if(openDay.equals(FacebookOperationHours.WORKING_SUNDAY)) {
					hoursMap.put("sun_1_open", openTime);
				}
				if(closeDay.equals(FacebookOperationHours.WORKING_SUNDAY)) {
					hoursMap.put("sun_1_close", closeTime);
				}
			}
			updateMask += "regularHours,";
		}
		
		if(hoursMap.isEmpty()) {
			hoursMap.putAll(getHoursMap(hours));
		}
		
		if (infoRequest.phoneNumber != null) {
			phone = infoRequest.phoneNumber;
			updateMask += "Phone,";
		}
		System.out.println("description : "+description);
		System.out.println("storeLocationDesc : "+storeLocationDesc);
		System.out.println("website : "+website);
		System.out.println("phone : "+phone);
		System.out.println("hoursMap : "+hoursMap);
		System.out.println("categories : "+categories);
		System.out.println("location : "+location);
		System.out.println("--------------------------------------------------------------");
		if(!testMode) {
			try {
				FacebookType response = fbClient.publish(page.getId(), FacebookType.class,
					Parameter.with("description", description),
					Parameter.with("website", website),
					Parameter.with("store_location_descriptor", storeLocationDesc),
					Parameter.with("hours", hoursMap),
					Parameter.with("category_list", categories),
			        Parameter.with("phone", phone),
			        Parameter.with("location", location));
					System.out.println("fb.com/"+response.toString());
			} catch (Exception e) {
				throw new RuntimeException("Failed to publish updates to Facebook page " + page.getId() + ": " + e.getMessage(), e);
			}
		}	
		
		if (updateMask.endsWith(",")) {
			updateMask = updateMask.substring(0, updateMask.length() - 1);
		}
		
		if (updateMask != null && !"".equalsIgnoreCase(updateMask.trim())) {
			if (logger != null && testMode == true) {
				logger.info("\t\t\t" + fbLocation.getDealerId() + "\t\t\t" + FacebookLocation.LOCATION_SOURCE_FACEBOOK
						+ "\t\t\t" + updateMask + "\t\t\tUpdate");
			}
		}
		return fbLocation;
	}
	
	private Map<String, String> getHoursMap(Optional<FacebookOperationHours> fbOperationHours) {
	    Map<String, String> hours = new HashMap<>();

	    fbOperationHours.ifPresent(foh -> {
	        putIfNotEmpty(hours, "mon_1_open", foh.getMondayOpenTime());
	        putIfNotEmpty(hours, "mon_1_close", foh.getMondayCloseTime());

	        putIfNotEmpty(hours, "tue_1_open", foh.getTuesdayOpenTime());
	        putIfNotEmpty(hours, "tue_1_close", foh.getTuesdayCloseTime());

	        putIfNotEmpty(hours, "wed_1_open", foh.getWednesdayOpenTime());
	        putIfNotEmpty(hours, "wed_1_close", foh.getWednesdayCloseTime());

	        putIfNotEmpty(hours, "thu_1_open", foh.getThursdayOpenTime());
	        putIfNotEmpty(hours, "thu_1_close", foh.getThursdayCloseTime());

	        putIfNotEmpty(hours, "fri_1_open", foh.getFridayOpenTime());
	        putIfNotEmpty(hours, "fri_1_close", foh.getFridayCloseTime());

	        putIfNotEmpty(hours, "sat_1_open", foh.getSaturdayOpenTime());
	        putIfNotEmpty(hours, "sat_1_close", foh.getSaturdayCloseTime());

	        putIfNotEmpty(hours, "sun_1_open", foh.getSundayOpenTime());
	        putIfNotEmpty(hours, "sun_1_close", foh.getSundayCloseTime());
	    });

	    return hours;
	}

	private void putIfNotEmpty(Map<String, String> map, String key, String value) {
	    if (StringUtils.isNotEmpty(value)) {
	        map.put(key, value);
	    }
	}

	public List<FacebookInfoQueue> findByStatus(String statusSubmit) {
		return facebookInfoQueueRepository.findByStatus(statusSubmit);
	}

}
