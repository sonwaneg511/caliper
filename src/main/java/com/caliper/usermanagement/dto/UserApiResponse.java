package com.caliper.usermanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserApiResponse {
	private int status;
	private String error;
	private String message;
	private LocalDateTime timestamp;
	private CreateUserResponseDto createUserResponseDto;
	private List<NewUserResponseDto> users;
}