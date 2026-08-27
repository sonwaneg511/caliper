package com.caliper.post.dto.Response;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostLocationDetailsResponse {
	
	@JsonProperty("post_id")
	private Long postId;

	@JsonProperty("post_type")
	private String postType;

	@JsonProperty("offer_title")
	private String offerTitle;
	
	@JsonProperty("label")
	private String label;

	@JsonProperty("summary")
	private String summary;

	@JsonProperty("start_date")
	private Date startDate;

	@JsonProperty("end_date")
	private Date endDate;

	@JsonProperty("image_url")
	private String imageUrl;

	@JsonProperty("media_format")
	private String mediaFormat;

	@JsonProperty("action_type")
	private String actionType;

	@JsonProperty("action_url")
	private String actionUrl;

	@JsonProperty("coupon_code")
	private String couponCode;

	@JsonProperty("redeem_url")
	private String redeemUrl;

	@JsonProperty("terms_conditions")
	private String termsConditions;


	@JsonProperty("created_by")
	private String createdBy;

	@JsonProperty("created_date")
	private Date createdDate;

	@JsonProperty("comment")
	private String comment;
	
	@JsonProperty("post_location_details")
	List<PostViewDetailsResponse> postViewDetailsResponseList;

}
