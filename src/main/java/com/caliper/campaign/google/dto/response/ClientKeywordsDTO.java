package com.caliper.campaign.google.dto.response;

import java.util.List;
import java.util.Map;

import com.caliper.campaign.google.entity.BaseKeywords;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientKeywordsDTO {

	@JsonProperty("client_id")
    private String clientId;
 
	@JsonProperty("sub_industry_keywords")
    private Map<String, List<BaseKeywords>> subIndustryKeywords;

	@JsonProperty("url_keywords")
	private Map<String, List<BaseKeywords>> urlKeywords;
}