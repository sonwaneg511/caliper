package com.caliper.reporting.dto.response;

import com.caliper.location.gmb.dto.response.GMBOperationHoursResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMBLocationReportResponse {

	@JsonProperty("location_name")
	private String locationName;

	@JsonProperty("status")
	private String status;

	@JsonProperty("campaign_setup")
	private boolean campaignSetup;

	@JsonProperty("health_score")
	private int healthScore;

	@JsonProperty("email_id")
	private String emailId;

	@JsonProperty("latitude")
	private String latitude;

	@JsonProperty("longitude")
	private String longitude;

	@JsonProperty("phone_number")
	private String phoneNumber;

	@JsonProperty("area")
	private String area;

	@JsonProperty("city")
	private String city;

	@JsonProperty("state")
	private String state;

	@JsonProperty("country_code")
	private String countryCode;

	@JsonProperty("pincode")
	private Long pincode;

	@JsonProperty("address1")
	private String address1;

	@JsonProperty("website_url")
	private String websiteUrl;

	@JsonProperty("campaign_phone_number")
	private String campaignPhoneNumber;

	@JsonProperty("description")
	private String description;

	@JsonProperty("gmb_operation_hours")
	private GMBOperationHoursResponse gmbOperationHours;

	@JsonProperty("landing_page_url")
	private String landingPageUrl;

	@JsonProperty("youtube_url")
	private String youtubeUrl;

	@JsonProperty("appointment_link")
	private String appointmentLink;

	@JsonProperty("whatsapp_url")
	private String whatsappUrl;

}
