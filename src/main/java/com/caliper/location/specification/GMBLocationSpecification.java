package com.caliper.location.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.review.dto.request.ReviewRequest;

import jakarta.persistence.criteria.Predicate;

public class GMBLocationSpecification {

	public static Specification<GMBLocation> filterLocations(LocationFilterRequest req){

		return (root, query, cb)->{

			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.equal(root.get("clientId"), req.getClientId()));

			if(req.getState() != null && !req.getState().isEmpty()) {
				predicates.add(cb.equal(root.get("state"), req.getState()));
			}

			if (req.getCity() != null && !req.getCity().isEmpty()) {
				predicates.add(cb.equal(root.get("city"), req.getCity()));
			}

			if (req.getDealerId() != null && !req.getDealerId().isEmpty()) {
				predicates.add(cb.equal(root.get("dealerId"), req.getDealerId()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};

	}
}
