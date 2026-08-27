package com.caliper.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.review.entity.DealerReviewAutoResponse;

public interface DealerReviewAutoResponseRepository extends JpaRepository<DealerReviewAutoResponse, Long>{
	
	List<DealerReviewAutoResponse> findByClientIdAndSourceAndRatingAndWithCommentOrderByRatingDesc(
	        String clientId, String source, Long rating, Boolean withComment);

}
