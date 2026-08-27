package com.caliper.campaign.google.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientData {

	@JsonProperty("client_names")
	private String clientName; 
	
	@JsonProperty("client_id")
	private String clientId; 
}
