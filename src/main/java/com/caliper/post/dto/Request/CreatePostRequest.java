package com.caliper.post.dto.Request;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CreatePostRequest {
	
	@JsonProperty("dealer_id")
	public List<String>dealerId;
	
	@JsonProperty("user_id")
	public String userId;
	
	@JsonProperty("action_type")
	public String actionType;
	
	@JsonProperty("action_url")
	public String actionURL;
	
	@JsonProperty("client_id")
	public String clientId;
	
	public String comment;
	
	@JsonProperty("coupon_code")
	public String couponCode;
	
	@JsonProperty("created_by")
	public String createdBy;
	
	@JsonProperty("created_date")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date createdDate;
	
	@JsonProperty("start_date")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date startDate;
	
	@JsonProperty("end_date")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date endDate;
	
	@JsonProperty("image_url")
	public String imageURL;
	
	public String label;
	
	@JsonProperty("media_format")
	public String mediaFormat;
	
	@JsonProperty("offer_title")
	public String offerTitle;
	
	public String platform;
	
	@JsonProperty("post_type")
	public String postType;
	
	@JsonProperty("redeem_url")
	public String redeemURL;
	
	public String status;
	
	public String summary;
	
	@JsonProperty("terms_conditions")
	public String termsConditions;

}
