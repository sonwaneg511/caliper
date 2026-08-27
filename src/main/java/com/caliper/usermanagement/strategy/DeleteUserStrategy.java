package com.caliper.usermanagement.strategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.mortbay.jetty.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.usermanagement.dto.UserApiResponse;
import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;
import com.caliper.usermanagement.rule.CheckAdminRule;
import com.caliper.usermanagement.rule.DeleteUserRule;
import com.caliper.usermanagement.rule.Rule;
import com.caliper.usermanagement.rule.UserStatusRule;
import com.caliper.usermanagement.service.UserService;

@Component
public class DeleteUserStrategy implements UserActionStrategy<UserApiResponse> {

	@Autowired
	private UserService userService;
	
	private final CheckAdminRule checkAdminRule;
	private final UserStatusRule userStatusRule;
	private final DeleteUserRule deleteUserRule;

	private static List<Rule> ruleList = new ArrayList<Rule>();

 	public DeleteUserStrategy(CheckAdminRule checkAdminRule, UserStatusRule userStatusRule, DeleteUserRule  deleteUserRule) {
		this.checkAdminRule = checkAdminRule;
		this.userStatusRule = userStatusRule;
		this.deleteUserRule = deleteUserRule;
		
		this.ruleList = List.of(
				checkAdminRule,
				userStatusRule,
				deleteUserRule
				);
	}

	@Override
	public UserApiResponse process(UserActionRequest request, UserActionContext context) {
		ruleList.forEach(r -> r.validate(request, context));
		userService.deleteUser(request.getTargetUserId(), request.getClientId());

		return new UserApiResponse(HttpStatus.ORDINAL_200_OK,"","User Deleted Succesfully", LocalDateTime.now(), null, null);
	}
}
