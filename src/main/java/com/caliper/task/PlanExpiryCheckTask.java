package com.caliper.task;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.caliper.job.runtime.ExecutableJob;
import com.caliper.planmanagement.entity.Plan;
import com.caliper.planmanagement.repository.PlanRepository;

public class PlanExpiryCheckTask implements ExecutableJob{

	@Autowired
	public PlanRepository planRepository;
	
	@Override
	public void run(Map<String, String> params) throws Exception {
		logStart(params);
		checkPlanExpiryStatus(params);
		logEnd();
	}

	private void checkPlanExpiryStatus(Map<String, String> params) {
		log("Job Started");
		Date currentDate = new Date();
		List<Plan> allPlans = planRepository.findAll();
		
		for(Plan plan : allPlans) {
			log("Fetching Plan for client id: " + plan.getClientId());
			Date startDate = plan.getStartDate();
			Date endDate = plan.getEndDate();
			
			if ((currentDate.before(startDate) || currentDate.after(endDate)) && !Plan.STATUS_EXPIRED.equals(plan.getStatus())) {
	            plan.setStatus(Plan.STATUS_EXPIRED);
	            planRepository.save(plan);
	        }
		}
		log("Job Executed");
	}

}
