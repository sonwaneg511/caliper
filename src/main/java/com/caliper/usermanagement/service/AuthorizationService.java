package com.caliper.usermanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.planmanagement.entity.Plan;
import com.caliper.planmanagement.entity.ServicePlanMapping;
import com.caliper.planmanagement.repository.PlanRepository;
import com.caliper.planmanagement.repository.ServicePlanMappingRepository;
import com.caliper.usermanagement.entity.Roles;
import com.caliper.usermanagement.entity.UserRoleClientMapping;
import com.caliper.usermanagement.repository.UserClientLocMappingRepository;

@Service("authorizationService")
public class AuthorizationService {

    @Autowired
    private UserService userService;
    
    @Autowired
	public PlanRepository planRepository;
   
    @Autowired
	public ServicePlanMappingRepository servicePlanMappingRepository;

    @Autowired
    private UserClientLocMappingRepository userClientLocMappingRepository;

    public boolean hasAccessToService(String clientId, String userId, String service)
    {
    	System.out.println("Inside Access Service- clientId:"+clientId+" - user_id = "+userId);

    	//Checking the services purchased in the plan
    	Plan findByClientId = planRepository.findByClientId(clientId);
    	
    	long planId = findByClientId.getId();
    	List<ServicePlanMapping> servicePlanMappingList = servicePlanMappingRepository.findByPlanId(planId);    	
    	
    	boolean servicePlanMatch = servicePlanMappingList.stream().
    		anyMatch(servicePlanMap -> servicePlanMap.getServiceKey().equalsIgnoreCase(service));
    	
    	//if the service exists in the plan check if the user has access to the module
    	System.out.println("Inside Access Service- servicePlanMatch:"+servicePlanMatch);
    	if(servicePlanMatch) {
    		
	    	//Checking the service/module access the user has
	        List<UserRoleClientMapping> userRoleClientMappings = userService.getUserRoleClientMappingByUserId(userId);
	        boolean containsService = userRoleClientMappings.stream().
	                anyMatch(userRoleClientMapping ->
	                        userRoleClientMapping.getRole().equalsIgnoreCase(service) ||
	                        userRoleClientMapping.getRole().equalsIgnoreCase(Roles.ADMIN.name()) ||
	                        userRoleClientMapping.getRole().equalsIgnoreCase(Roles.SUPER_ADMIN.name()));
	        
	    	System.out.println("Inside Access Service- containsService:"+containsService);

	        return containsService;
    	}else {
    		//else return false if the service is not part of the plan
	    	System.out.println("Inside Access Service- plan doesnt match:");

    		return false;
    	}
    }

    public boolean hasAccessToLocations(String clientId, String userId, List<String> dealerIds) {
    	System.out.println("Inside Access Locations- dealerIds:"+dealerIds);

    	if(dealerIds != null) {
    		  for (String dealerId : dealerIds) {
    	            boolean exists = userClientLocMappingRepository.existsByUserIdAndDealerId(userId, dealerId);
    	            if (!exists) {
    	                return false; // if any dealerId is not accessible, deny access
    	            }
    	        }
    	}else {
    		return true;
    	}
      
        return true; // all dealerIds accessible
    }

}

