package com.caliper.usermanagement.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserDetails {
	
	@JsonProperty("client_id")
	private String clientId;
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("dealer_id")
	private String dealerId;
	
	@JsonProperty("search_text")
	private String searchText;
	
	@JsonProperty("page_no")
	private int pageNo;
}
