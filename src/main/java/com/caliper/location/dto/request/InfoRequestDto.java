package com.caliper.location.dto.request;

import java.util.List;
import com.caliper.location.dto.CategoryServices;
import com.caliper.location.gmb.dto.HoursOfOperationRequest;
import lombok.Data;

@Data

public class InfoRequestDto {

	public Long gmbPrimaryCategory;
	public List<Long> gmbAdditionalCategories;
	public String appointmentLink;
	public String languageCode;
	public CategoryServices categoryServices;
	public String instagramAttribute;
	public String facebookAttribute;
	public String youtubeAttribute;
	public String twitterAttribute;
	public String whatsAppAttribute;
	public String linkedinAttribute;
	public Long fbPrimaryCategory;
	public List<Long> fbAdditionalCategories;
	public String area;
	public String storeLocationDescriptor;
	public String city;
	public String state;
	public String latitude;
	public String longitude;
	public String pincode;
	public String country;
	public String address;
	public String address1;
	public String address2;
	public String address3;
	public String locationTitle;
	public String websiteUrl;
	public String description;
	public List<String> labels;
	public HoursOfOperationRequest operationHours;
	public String phoneNumber;
	public List<String> additionalPhones;
// Campaign settings
	private String industry;
	private String subIndustry;
	private Double radius;
	private String clientCampaignPhoneNumber;
	private String callAdsPhoneNumber;
	private String landingPageUrl;
	private String youtubeUrl;
	
}
