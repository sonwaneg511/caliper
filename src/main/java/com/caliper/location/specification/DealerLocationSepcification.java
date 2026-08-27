package com.caliper.location.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;

import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.entity.DealerLocation;

import jakarta.persistence.criteria.Predicate;

public class DealerLocationSepcification {

	public static Specification<DealerLocation> filterLocations(LocationFilterRequest req, List<String> validDealerIds){

		return (root, query, cb)->{

			List<Predicate> predicates = new ArrayList<>();

			if (req.getClientId() != null) {
				predicates.add(cb.equal(root.get("clientId"), req.getClientId()));
			}

			//dealer id filter
			if (validDealerIds != null && !validDealerIds.isEmpty()) {
				predicates.add(root.get("dealerId").in(validDealerIds));
			}
			if(req.getState() != null && !req.getState().isEmpty()) {
				predicates.add(cb.equal(root.get("state"), req.getState()));
			}

			if (req.getCity() != null && !req.getCity().isEmpty()) {
				predicates.add(cb.equal(root.get("city"), req.getCity()));
			}
			if (req.getCountry() != null && !req.getCountry().isEmpty()) {
				predicates.add(cb.equal(root.get("country"), req.getCountry()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};

	}
	
	public static Specification<DealerLocation> filterDropdownLocations(LocationFilterRequest req, List<String> validDealerIds, boolean applyCountry, boolean applyState, boolean applyCity) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Mandatory filters
            predicates.add(cb.equal(root.get("clientId"), req.getClientId()));
            predicates.add(root.get("dealerId").in(validDealerIds));

            // Country
            if (applyCountry && req.getCountry() != null && !req.getCountry().isEmpty()) {
                predicates.add(cb.equal(root.get("country"), req.getCountry()));
            }

            // State
            if (applyState && req.getState() != null && !req.getState().isEmpty()) {
                predicates.add(cb.equal(root.get("state"), req.getState()));
            }

            // City
            if (applyCity && req.getCity() != null && !req.getCity().isEmpty()) {
                predicates.add(cb.equal(root.get("city"), req.getCity()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
	
}
