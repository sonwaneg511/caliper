package com.caliper.usermanagement.strategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.mortbay.jetty.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.usermanagement.dto.NewUserResponseDto;
import com.caliper.usermanagement.dto.UserApiResponse;
import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;
import com.caliper.usermanagement.rule.CheckAdminRule;
import com.caliper.usermanagement.rule.Rule;
import com.caliper.usermanagement.rule.UserPlanStatusRule;
import com.caliper.usermanagement.rule.UserStatusRule;
import com.caliper.usermanagement.service.UserService;

@Component
public class GetUserStrategy implements UserActionStrategy<UserApiResponse> {

	@Autowired
	private UserService userService;
	
	private final CheckAdminRule checkAdminRule;
	private final UserStatusRule userStatusRule;
	private final UserPlanStatusRule userPlanStatusRule;
	
	private static List<Rule> ruleList = new ArrayList<Rule>();
	
	public GetUserStrategy(CheckAdminRule checkAdminRule, UserStatusRule userStatusRule, UserPlanStatusRule userPlanStatusRule) {
		this.checkAdminRule = checkAdminRule;
        this.userStatusRule = userStatusRule;
        this.userPlanStatusRule = userPlanStatusRule;
		
		this.ruleList  = List.of(
				checkAdminRule,
				userStatusRule,
				userPlanStatusRule
				);
	}
	
	@Override
	public UserApiResponse process(UserActionRequest request, UserActionContext context) {
		ruleList.forEach(r -> r.validate(request, context));
		
		List<NewUserResponseDto> userResponseList = userService.viewAllUsers(request.getCurrentUserId(), request.getClientId());
		
		return new UserApiResponse(HttpStatus.ORDINAL_200_OK,"","List of All Users", LocalDateTime.now(), null, userResponseList);
	}

}
