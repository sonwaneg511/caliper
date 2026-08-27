package com.caliper.review.specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import com.caliper.review.dto.request.ReviewRequest;
import com.caliper.review.entity.GMBReview;

public class GMBReviewSpecification {

	public static Specification<GMBReview> filterReviews(ReviewRequest req, List<String> dealerIds){

		////////--------------------------code review: use constants -> clientId,dealerId,starRating,replyStatus,createdTime--------------------------
		if (dealerIds == null || dealerIds.isEmpty()) {
		    return (root, query, cb) -> cb.disjunction();
		}
		
		return (root, query, cb) -> {
			
			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.equal(root.get(ReviewRequest.CLIENT_ID), req.getClientId()));
			//dealer id filter

			if (dealerIds != null && !dealerIds.isEmpty()) {
				predicates.add(root.get(ReviewRequest.DEALER_ID).in(dealerIds));
			}
			//rating filter
			if (req.getRatingRange() > 0) {
				predicates.add(cb.equal(root.get(ReviewRequest.STAR_RATING), req.getRatingRange()));
			}

			// replied or not
			if (req.getReplied() != null && !req.getReplied().isEmpty()) {
				predicates.add(cb.equal(root.get(ReviewRequest.REPLY_STATUS), req.getReplied()));
			}

			// date range
			if (req.getStartDate() != null && req.getEndDate() != null) {
				predicates.add(cb.between(
						root.get(ReviewRequest.CREATED_TIME), req.getStartDate(), req.getEndDate()));
			}

			// rating type range
			if (req.getRatingType() != null && !req.getRatingType().isEmpty()) {
				if (req.getRatingType().equals(ReviewRequest.REVIEW_WITH_COMMENT)) {
					predicates.add(cb.isTrue(root.get(ReviewRequest.REVIEW_STATUS)));
				} else if (req.getRatingType().equals(ReviewRequest.REVIEW_WITHOUT_COMMENT)) {
					predicates.add(cb.isFalse(root.get(ReviewRequest.REVIEW_STATUS)));
				}
			}
						
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}


}
