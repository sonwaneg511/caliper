package com.caliper.campaign.google.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignFilterRequest {

	@JsonProperty("client_id")
	@JsonAlias({"clientId"})
	public String clientId;

	@JsonProperty("user_id")
	@JsonAlias({"userId"})
	public String userId;

	public String country;

	public String state;

	public String city;

	@JsonProperty("dealer_id")
	@JsonAlias({"dealerId"})
	public List<String> dealerId;

	@JsonProperty("page_no")
	@JsonAlias({"pageNo", "pageNumber", "page"})
	public int pageNo;

	public String search;
}
