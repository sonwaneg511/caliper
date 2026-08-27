package com.caliper.review.dto.response;

import java.util.List;

import com.caliper.review.entity.FacebookReview;
import com.caliper.review.entity.GMBReview;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookReviewResponse {

	private String pageToken;
	private List<FacebookReview> reviews;
}
