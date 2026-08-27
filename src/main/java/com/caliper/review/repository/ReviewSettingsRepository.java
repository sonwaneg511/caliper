package com.caliper.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caliper.review.entity.GMBReview;
import com.caliper.review.entity.ReviewSettings;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewSettingsRepository extends JpaRepository<ReviewSettings, Long>{

	ReviewSettings findByClientIdAndPlatform(String clientId, String platform);
	
}
