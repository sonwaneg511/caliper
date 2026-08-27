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
import com.caliper.utils.exception.customException.InvalidRequestException;

@Component
public class UserRoleCheckRule implements Rule{

	@Autowired
	public PlanRepository planRepository;
	
	@Autowired
	public ServicePlanMappingRepository servicePlanMappingRepository;
	
	@Autowired
	public PlanService planService;
	
	@Override
	public void validate(UserActionRequest request, UserActionContext context) {

	    if (request == null || context == null) {
	        throw new InvalidRequestException("Request or context cannot be null");
	    }

	    List<String> currRoles = context.getCurrentUserRole();

	    if (currRoles == null || currRoles.isEmpty()) {
	        throw new SecurityException("User does not have any roles");
	    }

	    	List<String> allowedroles = planService.getAllowedRoles(request.getClientId());
			
	    	//do conditioning for admin and super admin here
	    	if(currRoles.contains(UserRoleClientMapping.ROLE_ADMIN) || currRoles.contains(UserRoleClientMapping.ROLE_SUPER_ADMIN)) {
	    		currRoles = allowedroles;
	    	}
			
			boolean isSubset = allowedroles.containsAll(currRoles);
			
			if(!isSubset) {
				throw new SecurityException("User does not have all required roles");
			}
	}
}
