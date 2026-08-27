package com.caliper.post.Specification.PostLocationMapSpecification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.caliper.post.dto.Request.PostRequest;
import com.caliper.post.entity.PostLocationMap;

import jakarta.persistence.criteria.Predicate;

public class PostLocationMapSpecification {

	public static Specification<PostLocationMap> filterPostLocationMap(PostRequest req, List<String> dealerIds) {

		return (root, query, cb) -> {

			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.equal(root.get("clientId"), req.getClientId()));
			if (dealerIds != null && !dealerIds.isEmpty()) {
				predicates.add(root.get("dealerId").in(dealerIds));
			}else {
				predicates.add(cb.disjunction());
			}
			if (req.getStatus() != null && !req.getStatus().isEmpty()) {
				predicates.add(cb.equal(root.get("status"), req.getStatus()));
			}

			if (req.getPostId() != 0) {
				predicates.add(cb.equal(root.get("postId"), req.getPostId()));
			}
			
			if (req.getPlatform() != null && !req.getPlatform().isEmpty()) {
				predicates.add(cb.equal(root.get("platform"), req.getPlatform()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
	
	
	public static Specification<PostLocationMap> filterPostLocationMapForGarphData(
	        PostRequest req,
	        List<String> dealerIds,
	        List<Long> postIds) {

	    return (root, query, cb) -> {

	        List<Predicate> predicates = new ArrayList<>();

	        predicates.add(
	                cb.equal(root.get("clientId"), req.getClientId())
	        );

	        // postId IN (...)
	        if (postIds != null) {
	            if (!postIds.isEmpty()) {
	                predicates.add(
	                        root.get("postId").in(postIds)
	                );
	            } else {
	                // Caller computed a real (empty) result set => must match nothing
	                predicates.add(cb.disjunction());
	            }
	        }

	        // dealerId IN (...)
	        if (dealerIds != null && !dealerIds.isEmpty()) {
	            predicates.add(
	                    root.get("dealerId").in(dealerIds)
	            );
	        } else {
	            // No dealer IDs => return no records
	            predicates.add(cb.disjunction());
	        }

	        if (req.getStatus() != null && !req.getStatus().isEmpty()) {
	            predicates.add(
	                    cb.equal(root.get("status"), req.getStatus())
	            );
	        }

	        if (req.getPostId() != 0) {
	            predicates.add(
	                    cb.equal(root.get("postId"), req.getPostId())
	            );
	        }

	        if (req.getPlatform() != null && !req.getPlatform().isEmpty()) {
	            predicates.add(
	                    cb.equal(root.get("platform"), req.getPlatform())
	            );
	        }

	        return cb.and(predicates.toArray(new Predicate[0]));
	    };
	}

}
