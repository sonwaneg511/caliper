package com.caliper.reporting.specification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.caliper.post.dto.Request.PostRequest;
import com.caliper.post.entity.PostLocationMap;
import com.caliper.reporting.dto.request.InsightRequest;
import com.caliper.reporting.entity.GMBLocationInsight;

import jakarta.persistence.criteria.Predicate;

public class GMBInsightSpecification {
	

		public static Specification<GMBLocationInsight> filterGMBLocationInsight(InsightRequest req, Set<String> dealerIds) {

			return (root, query, cb) -> {

				List<Predicate> predicates = new ArrayList<>();

				predicates.add(cb.equal(root.get("clientId"), req.getClientId()));
				
				if (dealerIds != null && !dealerIds.isEmpty()) {
					predicates.add(root.get("dealerId").in(dealerIds));
				}else {
					predicates.add(cb.disjunction());
				}
				
				if (req.getStartDate() != null && req.getEndDate() != null) {

				    Calendar startCal = Calendar.getInstance();
				    startCal.setTime(req.getStartDate());
				    startCal.set(Calendar.HOUR_OF_DAY, 0);
				    startCal.set(Calendar.MINUTE, 0);
				    startCal.set(Calendar.SECOND, 0);
				    startCal.set(Calendar.MILLISECOND, 0);

				    Calendar endCal = Calendar.getInstance();
				    endCal.setTime(req.getEndDate());
				    endCal.set(Calendar.HOUR_OF_DAY, 23);
				    endCal.set(Calendar.MINUTE, 59);
				    endCal.set(Calendar.SECOND, 59);
				    endCal.set(Calendar.MILLISECOND, 999);

				    predicates.add(
				            cb.greaterThanOrEqualTo(
				                    root.get("reportDate"),
				                    startCal.getTime()
				            )
				    );

				    predicates.add(
				            cb.lessThanOrEqualTo(
				                    root.get("reportDate"),
				                    endCal.getTime()
				            )
				    );
				}
				

				return cb.and(predicates.toArray(new Predicate[0]));
			};
		}



}
