package com.caliper.post.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;

import com.caliper.post.dto.Request.PostRequest;
import com.caliper.post.entity.Post;

import jakarta.persistence.criteria.Predicate;

public class PostSpecification {

    public static Specification<Post> filterPost(PostRequest req, Set<Long> postIds) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("clientId"), req.getClientId()));
                        
            if (postIds != null && !postIds.isEmpty()) {
				predicates.add(root.get("postId").in(postIds));
			}

//            if (req.getClientId() != null && !req.getClientId().isEmpty()) {
//                predicates.add(cb.equal(root.get("clientId"), req.getClientId()));
//            }
//
			if (req.getStatus() != null && !req.getStatus().isEmpty()) {
			predicates.add(cb.equal(root.get("status"), req.getStatus()));
		}
            if (req.getStartDate() != null && req.getEndDate() != null) {
            	predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), req.getStartDate()));
                predicates.add(cb.lessThanOrEqualTo(root.get("createdDate"), req.getEndDate()));
            }
            if (req.getPlatform() != null && !req.getPlatform().isEmpty()) {
                predicates.add(cb.equal(root.get("platform"), req.getPlatform()));
            }

            return cb.and(predicates.toArray(new Predicate[0])); 
        };
    }
    
    public static Specification<Post> filterGarphPost(PostRequest req, List<String> dealerIds) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            
            if (dealerIds != null && !dealerIds.isEmpty()) {
				predicates.add(root.get("postId").in(dealerIds));
			}

            return cb.and(predicates.toArray(new Predicate[0])); 
        };
    }
}