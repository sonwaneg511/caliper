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
public class ForgotPasswordResponse {

	private boolean success;
	
	private String massage;
	
	@JsonProperty("token_value")
	private String tokenValue;
}
