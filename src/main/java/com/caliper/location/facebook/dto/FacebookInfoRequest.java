package com.caliper.location.facebook.dto;

import java.util.List;

import com.caliper.location.dto.request.LocalListingInfoRequest;
import com.caliper.location.facebook.entity.FacebookCategory;

public class FacebookInfoRequest extends LocalListingInfoRequest{

	public FacebookCategory fbPrimaryCategory;
	public List<FacebookCategory> fbAdditionalCategories;

	public String area;
	public String StoreLocationDescriptor;
	public String city;
	public String state;
	public String latitude;
	public String longitude;
	public String pincode;
	public String country;
	public String address;
}
