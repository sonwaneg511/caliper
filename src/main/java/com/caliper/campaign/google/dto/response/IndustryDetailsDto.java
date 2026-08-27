package com.caliper.campaign.google.dto.response;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndustryDetailsDto {

	@Expose
	private Map<String, List<String>> industryVsSubindustryMap;
	
}
