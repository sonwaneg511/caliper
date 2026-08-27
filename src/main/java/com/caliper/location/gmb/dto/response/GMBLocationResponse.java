package com.caliper.location.gmb.dto.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GMBLocationResponse {

	@JsonProperty("gmb_location_id")
	private String gmbLocationId;

	@JsonProperty("account_id")
	private String accountId;

	@JsonProperty("client_id")
	private String clientId;

	@JsonProperty("dealer_id")
	private String dealerId;

	@JsonProperty("name")
	private String name;

	@JsonProperty("address")
	private String address;

	@JsonProperty("area")
	private String area;

	@JsonProperty("city")
	private String city;

	@JsonProperty("state")
	private String state;

	@JsonProperty("pincode")
	private Long pincode;

	@JsonProperty("latitude")
	private String latitude;

	@JsonProperty("longitude")
	private String longitude;

	@JsonProperty("country_code")
	private String countryCode;

	@JsonProperty("language_code")
	private String languageCode;

	@JsonProperty("phone_number")
	private String phoneNumber;

	@JsonProperty("additional_phones")
	private String additionalPhones;

	@JsonProperty("map_url")
	private String mapUrl;

	@JsonProperty("new_review_url")
	private String newReviewUrl;

	@JsonProperty("open_info_can_reopen")
	private boolean openInfoCanReopen;

	@JsonProperty("open_info_status")
	private String openInfoStatus;

	@JsonProperty("website_url")
	private String websiteUrl;

	@JsonProperty("description")
	private String description;

	@JsonProperty("primary_category")
	private Long primaryCategory;

	@JsonProperty("additional_categories")
	private String additionalCategories;

	@JsonProperty("labels")
	private String labels;

	@JsonProperty("status")
	private String status;

	@JsonProperty("place_action_id")
	private String placeActionId;

	@JsonProperty("appointment_link")
	private String appointmentLink;

	@JsonProperty("address1")
	private String address1;

	@JsonProperty("address2")
	private String address2;

	@JsonProperty("address3")
	private String address3;

	@JsonProperty("inserted_date")
	private Date insertedDate;

	@JsonProperty("whatsapp_url")
	private String whatsappUrl;

	@JsonProperty("instagram_url")
	private String instagramUrl;

	@JsonProperty("youtube_url")
	private String youtubeUrl;

	@JsonProperty("facebook_url")
	private String facebookUrl;

	@JsonProperty("twitter_url")
	private String twitterUrl;

	@JsonProperty("linkedin_url")
	private String linkedinUrl;

	@JsonProperty("gmb_operation_hours")
	private GMBOperationHoursResponse gmbOperationHours;
}
