package com.caliper.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordRequest {

	@JsonProperty("forgot_password")
	private boolean forgotPassword;
	
	@JsonProperty("change_password")
	private boolean changePassword;
	
	@JsonProperty("password_token")
	private String passwordToken;
	
	@JsonProperty("password_verify_token")
	private String passwordVerifyToken;
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("new_password")
	private String newPassword;
	
	@JsonProperty("confirm_password")
	private String confirmPassword;
}
