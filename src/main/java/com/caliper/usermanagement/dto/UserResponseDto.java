package com.caliper.usermanagement.dto;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Data
public class UserResponseDto {
	
	private String userId;

	private String role;
	
	private List<String> modules;
	
	@JsonProperty("dealer_ids")
	private List<String> dealerIds;
	
	@JsonProperty("client_id")
	private String clientId;
	
	@JsonProperty("client_name")
	private String clientName;
	
	private int status;
	
	private String message;
	
	private String error;
	
	private boolean success;

	private String planStatus;

	@JsonProperty("account_status")
	private String accountStatus;

	@JsonProperty("email_verified")
	private boolean emailVerified;

	@JsonProperty("verification_link_expired")
	private boolean verificationLinkExpired;

	@JsonProperty("onboarding_step")
	private String onboardingStep;

	@JsonProperty("gmb_status")
	private String gmbStatus;

	@JsonProperty("meta_status")
	private String metaStatus;
	
	@JsonProperty("is_gmb_location_selected")
	private String locationStatus;
	
	@JsonProperty("total_locations")
	private long totalLocations;

	@JsonProperty("profile_completeness_score")
	private int profileCompletenessScore;

}
