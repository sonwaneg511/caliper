package com.caliper.usermanagement.rule;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;
import com.caliper.usermanagement.entity.UserRoleClientMapping;
import com.caliper.usermanagement.repository.UserRoleClientMappingRepository;
import com.caliper.utils.exception.customException.InvalidRequestException;

@Component
public class EditUserRule implements Rule{

	@Autowired
	public UserRoleClientMappingRepository userRoleClientMappingRepository;
	
	@Override
	public void validate(UserActionRequest request, UserActionContext context) {

	    if (request == null || context == null) {
	        throw new InvalidRequestException("Request or context cannot be null");
	    }

	    String currUserId = request.getCurrentUserId();
	    String targetUserId = request.getTargetUserId();

	    if (currUserId == null || targetUserId == null) {
	        throw new InvalidRequestException("User ids cannot be null");
	    }

	    List<String> currRolesList = context.getCurrentUserRole();
	    
	    List<UserRoleClientMapping> targetUserMappings =
	            userRoleClientMappingRepository.findByUserIdAndClientId(
	                    targetUserId, request.getClientId());

	    Set<String> targetRoles = targetUserMappings.stream()
	            .map(UserRoleClientMapping::getRole)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toSet());

	    boolean currIsAdmin = currRolesList.contains(UserRoleClientMapping.ROLE_ADMIN);
	    boolean currIsSuperAdmin = currRolesList.contains(UserRoleClientMapping.ROLE_SUPER_ADMIN);

	    boolean targetIsAdmin = targetRoles.contains(UserRoleClientMapping.ROLE_ADMIN);
	    boolean targetIsSuperAdmin = targetRoles.contains(UserRoleClientMapping.ROLE_SUPER_ADMIN);

	    //SUPER ADMIN can never be deleted
	    if (targetIsSuperAdmin) {
	        throw new SecurityException("Super Admin cannot be Edited");
	    }

	    //ADMIN cannot delete ADMIN
	    if (currIsAdmin && targetIsAdmin) {
	        throw new SecurityException("Admin is not allowed to Edit another Admin");
	    }

	    //ADMIN cannot delete SUPER ADMIN
	    if (currIsAdmin && targetIsSuperAdmin) {
	        throw new SecurityException("Admin is not allowed to Edit Super Admin");
	    }

	}


}
