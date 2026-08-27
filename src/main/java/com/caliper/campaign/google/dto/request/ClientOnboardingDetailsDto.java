package com.caliper.campaign.google.dto.request;

import java.util.List;

import com.caliper.campaign.google.entity.BaseKeywords;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ClientOnboardingDetailsDto {

	@JsonProperty("client_id")
	public String clientId;
	
	@JsonProperty("client_code")
	public long clientCode;
	
	@JsonProperty("google_account_id")
	public String googleAccountId;
	
	@JsonProperty("login_customer_id")
	public long loginCustomerId;
	
	@JsonProperty("sub_industry_keywords")
	public List<BaseKeywords>subIndustryKeywords;
	
	@JsonProperty("url_keywords")
	public List<BaseKeywords>urlKeywords;
	
}
