package com.caliper.location.gmb.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GMBOperationHoursResponse {

	@JsonProperty("monday_open_time")
	private String mondayOpenTime;

	@JsonProperty("monday_close_time")
	private String mondayCloseTime;

	@JsonProperty("tuesday_open_time")
	private String tuesdayOpenTime;

	@JsonProperty("tuesday_close_time")
	private String tuesdayCloseTime;

	@JsonProperty("wednesday_open_time")
	private String wednesdayOpenTime;

	@JsonProperty("wednesday_close_time")
	private String wednesdayCloseTime;

	@JsonProperty("thursday_open_time")
	private String thursdayOpenTime;

	@JsonProperty("thursday_close_time")
	private String thursdayCloseTime;

	@JsonProperty("friday_open_time")
	private String fridayOpenTime;

	@JsonProperty("friday_close_time")
	private String fridayCloseTime;

	@JsonProperty("saturday_open_time")
	private String saturdayOpenTime;

	@JsonProperty("saturday_close_time")
	private String saturdayCloseTime;

	@JsonProperty("sunday_open_time")
	private String sundayOpenTime;

	@JsonProperty("sunday_close_time")
	private String sundayCloseTime;
}
