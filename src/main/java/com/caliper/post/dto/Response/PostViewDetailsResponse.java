package com.caliper.post.dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostViewDetailsResponse {
	
	@JsonProperty("dealer_id")
	private String dealerId;
	@JsonProperty("location_name")
	private String locName;
	private String area;
	private String city;
	private String status;
	long likes;
	long comments;
	long shares;
	

}
