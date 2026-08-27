package com.caliper.location.dto.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DealerList {
	
	@JsonProperty("client_id")
	private String clientId;
	
	@JsonProperty("gmb_dealer_list")
	private List<GMBDealerList> gmbDealerList;
	
	@JsonProperty("facebook_dealer_list")
	private List<FacebookDealerList> facebookDealerList;
	
}
