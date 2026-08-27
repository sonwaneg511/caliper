package com.caliper.review.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewReplyRequest {
	
	@JsonProperty("client_id")
	public String clientId;
	
	@JsonProperty("user_id")
	public String userId;
	
	@JsonProperty("review_id")
	public String reviewId;
	
	@JsonProperty("review_comment")
	public String reviewComment;
	
	@JsonProperty("reply_comment")
	public String replyComment;
	
	@JsonProperty("platform")
	public String platform;

}
