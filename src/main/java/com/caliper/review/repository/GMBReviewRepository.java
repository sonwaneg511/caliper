package com.caliper.review.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.caliper.dashboard.dto.review.AggregatedReviewDTO;
import com.caliper.review.entity.GMBReview;

@Repository
public interface GMBReviewRepository extends JpaRepository<GMBReview, Long>, JpaSpecificationExecutor<GMBReview>{

	GMBReview findByReviewIdAndClientId(String reviewId, String clientId);
	
	List<GMBReview> findByClientIdAndReplyStatus(String clientId, String replyStatus);
	
	List<GMBReview> findByClientIdAndReplyStatusAndReviewStatus(String clientId, String replyStatus, boolean reviewStatus);

	List<GMBReview> findByClientIdAndDealerIdInAndCreatedTimeBetween(String clientId, Set<String>dealerIds, Date fromDate, Date toDate);

	  @Query("""
		        SELECT new com.caliper.dashboard.dto.review.AggregatedReviewDTO(
		            r.dealerId,
		            COUNT(r),
		            SUM(r.starRating),

		            SUM(CASE WHEN r.starRating = 5 THEN 1 ELSE 0 END),
		            SUM(CASE WHEN r.starRating = 4 THEN 1 ELSE 0 END),
		            SUM(CASE WHEN r.starRating = 3 THEN 1 ELSE 0 END),
		            SUM(CASE WHEN r.starRating = 2 THEN 1 ELSE 0 END),
		            SUM(CASE WHEN r.starRating = 1 THEN 1 ELSE 0 END),

		            SUM(CASE WHEN r.starRating >= 4 THEN 1 ELSE 0 END),
		            SUM(CASE WHEN r.starRating = 3 THEN 1 ELSE 0 END),
		            SUM(CASE WHEN r.starRating <= 2 THEN 1 ELSE 0 END),

		            YEAR(r.createdTime),
		            MONTH(r.createdTime)
		        )
		        FROM GMBReview r
		        WHERE r.clientId = :clientId
		          AND r.dealerId IN :dealerIds
		          AND r.createdTime BETWEEN :fromDate AND :toDate
		        GROUP BY r.dealerId, YEAR(r.createdTime), MONTH(r.createdTime)
		    """)
		    List<AggregatedReviewDTO> fetchAggregatedReviews(
		            @Param("clientId") String clientId,
		            @Param("dealerIds") Set<String> dealerIds,
		            @Param("fromDate") Date fromDate,
		            @Param("toDate") Date toDate
		    );
	  
	  @Query(value = """
		        SELECT *
		        FROM gmb_review
		        WHERE client_id = :clientId
		        AND created_time BETWEEN :startDate AND :endDate
		    """, nativeQuery = true)
		    List<GMBReview> findReviewsByClientAndDateRange(
		            @Param("clientId") String clientId,
		            @Param("startDate") Date startDate,
		            @Param("endDate") Date endDate
		    );
}
