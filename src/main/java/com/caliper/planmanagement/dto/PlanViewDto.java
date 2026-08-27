package com.caliper.planmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlanViewDto {

	private long id;
	
	@JsonProperty("service_name")
	private String serviceName;
}
