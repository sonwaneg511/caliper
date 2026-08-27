package com.caliper.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.caliper.location.facebook.entity.FacebookLocation;
import com.caliper.review.entity.FacebookReview;
import com.caliper.review.entity.GMBReview;

public interface FacebookReviewRepository extends JpaRepository<FacebookReview, Long>, JpaSpecificationExecutor<FacebookReview>{

	public List<FacebookReview> findByClientId(String clientId);
	
	FacebookReview findByReviewIdAndClientId(String reviewId, String clientId);

	public List<FacebookReview> findByClientIdAndReplyStatus(String clientName, String replyStatus);

}
