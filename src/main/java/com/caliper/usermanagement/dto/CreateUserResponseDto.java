package com.caliper.usermanagement.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateUserResponseDto {

	@JsonProperty("invaliduserIds")
	public List<String> invalidUserIds;
	
	public String message;
}
