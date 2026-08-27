package com.caliper.reporting.dto.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostReportingData {
	
	public Date date;
	public String title;
	@JsonProperty("created_by")
	public String createdBy;
	public String platform;
	public String postType;
	@JsonProperty("post_id")
	public long postId;
	@JsonProperty("dealer_id")
	public String dealerId;

	@JsonProperty("client_id")
	public String clientId;

	public String summary;

	@JsonProperty("start_date")
	public Date startDate;

	@JsonProperty("end_date")
	public Date endDate;

	@JsonProperty("image_url")
	public String imageUrl;

	@JsonProperty("media_format")
	public String mediaFormat;

	@JsonProperty("action_type")
	public String actionType;

	@JsonProperty("action_url")
	public String actionUrl;

	@JsonProperty("coupon_code")
	public String couponCode;

	@JsonProperty("redeem_url")
	public String redeemUrl;

	@JsonProperty("terms_conditions")
	public String termsConditions;

	@JsonProperty("post_label")
	public String postLabel;

	public String comment;

	public String status;
}
