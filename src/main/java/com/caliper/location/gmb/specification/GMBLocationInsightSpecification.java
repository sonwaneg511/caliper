package com.caliper.location.gmb.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.caliper.post.dto.Request.PostRequest;
import com.caliper.reporting.dto.request.GMBLocationInisghtRequest;
import com.caliper.reporting.entity.GMBLocationInsight;

import jakarta.persistence.criteria.Predicate;

public class GMBLocationInsightSpecification {

	public static Specification<GMBLocationInsight> filterGmbLocationInsight(GMBLocationInisghtRequest req) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            
            if (req.getDealerIds() != null && !req.getDealerIds().isEmpty()) {
				predicates.add(root.get("dealerId").in(req.getDealerIds()));
			}
            
            if (req.getClientId() != null && !req.getClientId().isEmpty()) {
    			predicates.add(cb.equal(root.get("clientId"), req.getClientId()));
    		}
            
            if (req.getStartDate() != null && req.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("reportDate"), req.getEndDate()));
                predicates.add(cb.greaterThanOrEqualTo(root.get("reportDate"), req.getStartDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0])); 
        };
    }
}
