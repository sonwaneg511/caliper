package com.caliper.location.gmb.dto.response;

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
public class GMBAccountResponse {

	@JsonProperty("account_id")
	private String accountId;

	@JsonProperty("account_name")
	private String accountName;

	@JsonProperty("client_id")
	private String clientId;

	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("locations")
	private List<GMBLocationResponse> locations;
}
