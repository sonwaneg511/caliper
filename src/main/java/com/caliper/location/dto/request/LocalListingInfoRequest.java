package com.caliper.location.dto.request;

import java.util.List;

import com.caliper.location.gmb.dto.HoursOfOperationRequest;

public class LocalListingInfoRequest {

	public String locationTitle;
	public String websiteUrl;
	public String description;
	public List<String> labels;
	public HoursOfOperationRequest operationHours;

	public String phoneNumber;
	public List<String> additionalPhones;
}
