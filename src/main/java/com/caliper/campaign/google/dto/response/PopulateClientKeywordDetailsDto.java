package com.caliper.campaign.google.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PopulateClientKeywordDetailsDto {

	@JsonProperty("client_names")
	private List<ClientData> clientNames; 
	
	@JsonProperty("client_keywords")
    private List<ClientKeywordsDTO> clientKeywords;
    
}