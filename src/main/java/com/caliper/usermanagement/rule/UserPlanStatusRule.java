package com.caliper.usermanagement.rule;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.planmanagement.repository.PlanRepository;
import com.caliper.planmanagement.repository.ServicePlanMappingRepository;
import com.caliper.planmanagement.service.PlanService;
import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;
import com.caliper.usermanagement.entity.UserRoleClientMapping;

@Component
public class UserPlanStatusRule implements Rule{

	@Autowired
	public PlanRepository planRepository;
	
	@Autowired
	public ServicePlanMappingRepository servicePlanMappingRepository;
	
	@Autowired
	public PlanService planService;
	
	@Override
	public void validate(UserActionRequest request, UserActionContext context) {
		
		List<String> currRoles = context.getCurrentUserRole();
		//List<String> reqRoles = request.getRoles();
		
			List<String> allowedroles = planService.getAllowedRoles(request.getClientId());
			
			if(currRoles.contains(UserRoleClientMapping.ROLE_ADMIN) || currRoles.contains(UserRoleClientMapping.ROLE_SUPER_ADMIN)) {
	    		currRoles = allowedroles;
	    	}
			
			boolean isSubset = allowedroles.containsAll(currRoles);
			
			if(!isSubset) {
				throw new SecurityException("User does not have all required roles");
			}
	}

}
