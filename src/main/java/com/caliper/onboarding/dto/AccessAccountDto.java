package com.caliper.onboarding.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessAccountDto {

	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("account_id")
	private String accountId;
	
	@JsonProperty("account_name")
	private String accountName;

	@JsonProperty("last_modified_by")
	private String lastModifiedBy;

	@JsonProperty("last_modified_date")
	private Date lastModifiedDate;
	
	@JsonProperty("facebook_page_ids")
	private List<Long> facebookPageIds;
}
