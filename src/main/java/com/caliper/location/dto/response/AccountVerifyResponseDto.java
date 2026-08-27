package com.caliper.location.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccountVerifyResponseDto {

	@JsonProperty(value = "exist")
	public boolean exist;
	
	@JsonProperty(value = "message")
	public String message;
	
	@JsonProperty(value = "account_name")
	public String accountName;
	
	@JsonProperty(value = "account_number")
	public String accountNumber;
}
