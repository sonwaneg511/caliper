package com.caliper.usermanagement.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;
import com.caliper.usermanagement.entity.UserRoleClientMapping;
import com.caliper.usermanagement.repository.UserRoleClientMappingRepository;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;

@Component
public class CheckAdminRule implements Rule{

	@Autowired
	private UserRoleClientMappingRepository userRoleClientMappingRepository;
	
	
	@Override
	public void validate(UserActionRequest request, UserActionContext context) {
		if(request.getCurrentUserId() == null || request.getClientId() == null) {
			throw new InvalidRequestException("Response or Request cannot be null");
		}
		
		
		 List<UserRoleClientMapping> mappings = userRoleClientMappingRepository.findByUserIdAndClientId(request.getCurrentUserId(), request.getClientId());
		 boolean accessAllow = false;
		 if(!mappings.isEmpty()) {
			 for(UserRoleClientMapping mapping : mappings) {
				 if(UserRoleClientMapping.ROLE_ADMIN.equalsIgnoreCase(mapping.getRole()) || UserRoleClientMapping.ROLE_SUPER_ADMIN.equalsIgnoreCase(mapping.getRole())) {
					 return;
				 }
				// set flag and give else statement
				 accessAllow = true;
			 }
			 if(accessAllow) {
				 throw new SecurityException("Access Denied for user : " + request.getCurrentUserId());
			 }
		 } else {
			 throw new ResourceNotFoundException("User Roles are empty");
		 }
	}
	
}