package com.caliper.review.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewReplyResponse {

	@JsonProperty("user_id")
	public String userId;
	
	@JsonProperty("review_id")
	public String reviewId;
	
	@JsonProperty("reply_comment")
	public String replyComment;
	
	public String success;
}
