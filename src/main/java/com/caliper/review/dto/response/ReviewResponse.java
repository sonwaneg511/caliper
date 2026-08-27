package com.caliper.review.dto.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

	@JsonProperty("reviewer_name")
	public String reviewerName;

	public long rating;

	public String comment;
	
	public Date posted_date;
	
	@JsonProperty("dealer_id")
	public String dealerId;
	
	@JsonProperty("store_name")
	public String storeName;
	
	@JsonProperty("store_address")
	public String storeAddress;
	
	@JsonProperty("reply_comment")
	public String replyComment;
	
	@JsonProperty("review_id")
	public String reviewId;
	
	@JsonProperty("reply_status")
	public String replyStatus;

}
