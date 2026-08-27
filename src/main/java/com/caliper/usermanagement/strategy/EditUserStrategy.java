package com.caliper.usermanagement.strategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.mortbay.jetty.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.usermanagement.dto.EditUserRequestDto;
import com.caliper.usermanagement.dto.UserApiResponse;
import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;
import com.caliper.usermanagement.rule.CheckAdminRule;
import com.caliper.usermanagement.rule.EditUserRule;
import com.caliper.usermanagement.rule.Rule;
import com.caliper.usermanagement.rule.UserLocationCheckRule;
import com.caliper.usermanagement.rule.UserPlanStatusRule;
import com.caliper.usermanagement.rule.UserRoleCheckRule;
import com.caliper.usermanagement.rule.UserStatusRule;
import com.caliper.usermanagement.service.UserService;

@Component
public class EditUserStrategy implements UserActionStrategy<UserApiResponse> {

	@Autowired
	private UserService userService;
	
	private final CheckAdminRule checkAdminRule;
	private final EditUserRule editUserRule;
	private final UserStatusRule userStatusRule;
	private final UserPlanStatusRule userPlanStatusRule;
	private final UserRoleCheckRule userRoleCheckRule;
	private final UserLocationCheckRule userLocationCheckRule;

	private static List<Rule> ruleList = new ArrayList<Rule>();

	public EditUserStrategy(CheckAdminRule checkAdminRule, EditUserRule editUserRule, UserStatusRule userStatusRule, UserPlanStatusRule userPlanStatusRule, UserRoleCheckRule userRoleCheckRule, UserLocationCheckRule userLocationCheckRule) {
		this.checkAdminRule = checkAdminRule;
		this.editUserRule = editUserRule;
        this.userStatusRule = userStatusRule;
        this.userPlanStatusRule = userPlanStatusRule;
        this.userRoleCheckRule = userRoleCheckRule;
        this.userLocationCheckRule = userLocationCheckRule;
		
		ruleList = List.of(
				checkAdminRule,
				editUserRule,
				userStatusRule,
				userPlanStatusRule,
				userRoleCheckRule,
				userLocationCheckRule
				);
	}

	@Override
	public UserApiResponse process(UserActionRequest request, UserActionContext context) {
		ruleList.forEach(r -> r.validate(request, context));

		EditUserRequestDto dto = new EditUserRequestDto();
		dto.setUserId(request.getTargetUserId());
		dto.setClientId(request.getClientId());
		dto.setDealerIds(request.getLocations());
		dto.setRoles(request.getRoles());

		userService.modifyUser(dto);
		return new UserApiResponse(HttpStatus.ORDINAL_200_OK, "", "User Updated successfully", LocalDateTime.now(), null, null);
	}
}
