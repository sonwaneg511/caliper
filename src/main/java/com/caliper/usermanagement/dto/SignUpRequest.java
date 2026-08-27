package com.caliper.usermanagement.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SignUpRequest {

	@JsonProperty("user_id")
    @NotBlank(message = "User ID is required")
    private String userId;

    @JsonProperty("user_name")
    private String userName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&^#()_+=\\-{}\\[\\]:;\"'<>,./~`|\\\\]).{8,64}$",
        message = "Password must contain at least 1 uppercase, 1 lowercase, 1 number, and 1 symbol"
    )
    private String password;
    
    @JsonProperty("country_code")
    @NotBlank(message = "Country code is required")
    private String countryCode;
    
    @JsonProperty("phone_number")
    @NotBlank(message = "Phone Number is required")
    private String phoneNumber;
    
    @JsonProperty("client_name")
    @NotBlank(message = "Client name is required")
    private String clientName;
}
