package com.caliper.location.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacebookDetailsBody {

//	public String businessName;
	public Long fbPrimaryCategory;
	public List<Long> fbAdditionalCategories;
	public String storeLocationDescriptor;
	public String area;
	public String city;
	public String state;
	public String pincode;
	public String country;
	public String address1;
	public String address2;
	public String address3;
	public String latitude;
	public String longitude;
}
