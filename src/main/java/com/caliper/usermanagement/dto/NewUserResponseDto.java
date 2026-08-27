package com.caliper.usermanagement.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NewUserResponseDto {
	@JsonProperty("user_name")
	private String userName;
	
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("role")
	private String role;
	
	@JsonProperty("modules")
	private List<String> modules;
	
	@JsonProperty("id")
	private long id;
	
	@JsonProperty("location_count")
	private long locationCount;
	
	@JsonProperty("created_by")
	private String createdBy;
}
