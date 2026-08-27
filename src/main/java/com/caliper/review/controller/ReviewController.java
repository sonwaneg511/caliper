package com.caliper.review.controller;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.caliper.review.dto.request.ReviewReplyRequest;
import com.caliper.review.dto.request.ReviewRequest;
import com.caliper.review.dto.response.ReviewGraphResponse;
import com.caliper.review.dto.response.ReviewPageResponse;
import com.caliper.review.dto.response.ReviewReplyResponse;
import com.caliper.review.service.ReviewService;


@RestController
@RequestMapping("review")
public class ReviewController {

	@Autowired
	private ReviewService reviewService;

	@PostMapping("/review-insight")
	public ResponseEntity<ReviewGraphResponse> getReviewInsights(@RequestBody ReviewRequest req){	
		ReviewGraphResponse reviewGraphResponse = reviewService.getReviewInsights(req);
		return ResponseEntity.ok(reviewGraphResponse);
	}

	@PostMapping("/filtered-reviews")
	public ResponseEntity<ReviewPageResponse> getReviewPageResponse(@RequestBody ReviewRequest req){
		return ResponseEntity.ok(reviewService.getReviewPageResponse(req));
	}

	@PostMapping("/reply-comment")
	public ResponseEntity<ReviewReplyResponse> postReviewReply(@RequestBody ReviewReplyRequest req) throws FileNotFoundException, IOException, SQLException {
			return ResponseEntity.ok(reviewService.postReviewReply(req));
	}

}
