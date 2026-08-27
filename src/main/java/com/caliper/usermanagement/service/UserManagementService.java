package com.caliper.usermanagement.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;
import com.caliper.usermanagement.entity.User;
import com.caliper.usermanagement.entity.UserClientLocMapping;
import com.caliper.usermanagement.entity.UserOperationEnum;
import com.caliper.usermanagement.entity.UserRoleClientMapping;
import com.caliper.usermanagement.repository.UserClientLocMappingRepository;
import com.caliper.usermanagement.repository.UserRepository;
import com.caliper.usermanagement.repository.UserRoleClientMappingRepository;
import com.caliper.usermanagement.rule.UserActionStrategyFactory;
import com.caliper.usermanagement.strategy.UserActionStrategy;
import com.caliper.utils.exception.customException.ResourceNotFoundException;

@Service
public class UserManagementService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserActionStrategyFactory userActionStrategyFactory;

	@Autowired
	private  UserRoleClientMappingRepository userRoleClientMappingRepository;
	
	@Autowired
	private UserClientLocMappingRepository userClientLocMappingRepository;
	
	public <R> R handle(UserOperationEnum action, UserActionRequest request) {

		// Load current user from DB
		User currentUser = userRepository.findByUserId(request.getCurrentUserId())
				.orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

		// Load target user if needed
		User targetUser = null;
		if (request.getTargetUserId() != null) {
			targetUser = userRepository.findByUserId(request.getTargetUserId())
					.orElseThrow(() -> new ResourceNotFoundException("Target user not found"));
		}

		List<UserRoleClientMapping> userRoleClientMappingList = userRoleClientMappingRepository.findByUserIdAndClientId(request.getCurrentUserId(), request.getClientId());
		List<String> currentUserRoleList = new ArrayList<>();
		
		for(UserRoleClientMapping mapping : userRoleClientMappingList) {
			currentUserRoleList.add(mapping.getRole());
		}
		
		List<UserClientLocMapping> userClientLocMappingList = userClientLocMappingRepository.findByUserIdAndclientId(request.getCurrentUserId(), request.getClientId());
		List<String> currentUserLocationList = new ArrayList<>();
		
		for(UserClientLocMapping locMapping : userClientLocMappingList) {
			currentUserLocationList.add(locMapping.getDealerId());
		}
		
		
		// Build Context
		UserActionContext context = new UserActionContext();
		context.setClientId(request.getClientId());
		context.setCurrentUser(currentUser);
		context.setTargetUserObj(targetUser);
		context.setTargetUser(request.getTargetUserId());
		context.setRoles(request.getRoles());
		context.setLocations(request.getLocations());
		context.setCurrentUserRole(currentUserRoleList);
		context.setCurrentUserLocations(currentUserLocationList);

		// Execute Strategy
		UserActionStrategy<R> strategy = userActionStrategyFactory.getStrategy(action);
		return strategy.process(request, context);
	}
}
