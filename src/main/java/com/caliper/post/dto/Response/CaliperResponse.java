package com.caliper.post.dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CaliperResponse {
	
	@JsonProperty("client_id")
	public String clientId;
	public String status;
	public String message;

}
