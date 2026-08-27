package com.caliper.review.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.caliper.review.dto.request.ReviewRequest;
import com.caliper.review.entity.FacebookReview;

import jakarta.persistence.criteria.Predicate;

public class FacebookReviewSpecification {

	public static Specification<FacebookReview> filterReviews(ReviewRequest req, List<String> dealerIds){

		if (dealerIds == null || dealerIds.isEmpty()) {
		    return (root, query, cb) -> cb.disjunction();
		}
		
		return (root, query, cb) -> {

			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.equal(root.get("clientId"), req.getClientId()));
			//dealer id filter
			if (dealerIds != null && !dealerIds.isEmpty()) {
				predicates.add(root.get("dealerId").in(dealerIds));
			}

			//rating filter
			if (req.getRatingRange() > 0) {
				predicates.add(cb.equal(root.get("starRating"), req.getRatingRange()));
			}

			// replied or not
			if (req.getReplied() != null && !req.getReplied().isEmpty()) {
				predicates.add(cb.equal(root.get("replyStatus"), req.getReplied()));
			}

			// date range
			if (req.getStartDate() != null && req.getEndDate() != null) {
				predicates.add(cb.between(
						root.get("createdTime"), req.getStartDate(), req.getEndDate()));
			}

			// rating type range
			if (req.getRatingType() != null && !req.getRatingType().isEmpty()) {
				if (req.getRatingType().equals(ReviewRequest.REVIEW_WITH_COMMENT)) {
					predicates.add(cb.isTrue(root.get("reviewStatus")));
				} else if (req.getRatingType().equals(ReviewRequest.REVIEW_WITHOUT_COMMENT)) {
					predicates.add(cb.isFalse(root.get("reviewStatus")));
				}
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

}
