package com.caliper.usermanagement.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateUserRequestDto {
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("created_by")
	private String createdBy;
	
	@JsonProperty("created_date")
	private Date createdDate;
	
	@JsonProperty("user_name")
	private String userName;
	
	@JsonProperty("password")
	private String password;
	
	@JsonProperty("client_id")
	private String clientId;
	
	@JsonProperty("dealer_ids")
	List<String>dealerIds;
	
	@JsonProperty("roles")
	List<String>roles;
	
	@JsonProperty("last_modified_by")
	private String lastModifiedBy;
	
	@JsonProperty("new_user")
	private List<String> newUser;
	
	@JsonProperty("action")
	private String action;
	
}
