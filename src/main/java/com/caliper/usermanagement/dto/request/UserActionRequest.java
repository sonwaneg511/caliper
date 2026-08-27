package com.caliper.usermanagement.dto.request;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserActionRequest {
	
	@JsonProperty("client_id")
	private String clientId;
	
	@JsonProperty("current_user_id")
    private String currentUserId;
    
	@JsonProperty("target_user_id")
    private String targetUserId;
	
	@JsonProperty("new_user_ids")
    private List<String> newUserIds;
	
    private List<String> roles;
    
    private List<String> locations;
    
    private String action;
}
