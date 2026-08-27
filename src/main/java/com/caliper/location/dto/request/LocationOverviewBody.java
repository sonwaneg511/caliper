package com.caliper.location.dto.request;

import java.util.List;

import com.caliper.location.gmb.dto.HoursOfOperationRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationOverviewBody {


	public List<String> labels;
	public String description;
	public HoursOfOperationRequest operationHours;
	public String phoneNumber;
	public List<String> additionalPhones;
	public String locationTitle;
	public String websiteUrl;
}