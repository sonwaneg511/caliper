package com.caliper.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LocationMediaDto {

	private Long id;
	private String url;
	private String category;
	private String platform;
	private String status;
}
