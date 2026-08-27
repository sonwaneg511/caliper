package com.caliper.usermanagement.dto.request;

import java.util.List;
import java.util.Set;

import com.caliper.usermanagement.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserActionContext {

	@JsonProperty("client_id")
	private String clientId;
	 
	@JsonProperty("current_user")
    private User currentUser;
	
	@JsonProperty("target_userObj")
    private User targetUserObj;
	
	@JsonProperty("target_user")
	private String targetUser;
 
    private List<String> roles;
    
    @JsonProperty("current_user_role")
	private List<String> currentUserRole;
    
    private List<String> locations;
    
    @JsonProperty("current_user_locations")
	private List<String> currentUserLocations;
}
