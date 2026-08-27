package com.caliper.location.dto.response;

import java.util.List;
import java.util.Map;

import com.caliper.location.gmb.entity.GMBLocation;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GMBAccountLocationsResponse {

//	@JsonProperty("account_id")
//	private String accountId;
//
//	@JsonProperty("account_name")
//	private String accountName;

	// Keyed by account_name. Names aren't guaranteed unique per client — if two accounts share a
	// name, their location lists are merged under that key (see GMBLocationService.importGMBLocation).
	@JsonProperty("gmb_locations")
	private Map<String, List<GMBLocation>> gmbLocations;

//	@JsonProperty("selected_accounts")
//	private List<SelectedAccount> selectedAccounts;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class SelectedAccount {

		@JsonProperty("account_id")
		private String accountId;

		@JsonProperty("account_name")
		private String accountName;
	}
}
