package com.caliper.usermanagement.strategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.mortbay.jetty.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.usermanagement.dto.CreateUserRequestDto;
import com.caliper.usermanagement.dto.CreateUserResponseDto;
import com.caliper.usermanagement.dto.UserApiResponse;
import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;
import com.caliper.usermanagement.rule.CheckAdminRule;
import com.caliper.usermanagement.rule.NewUserIdCheckRule;
import com.caliper.usermanagement.rule.Rule;
import com.caliper.usermanagement.rule.UserLocationCheckRule;
import com.caliper.usermanagement.rule.UserRoleCheckRule;
import com.caliper.usermanagement.rule.UserStatusRule;
import com.caliper.usermanagement.service.UserService;

@Component
public class InsertUserStrategy implements UserActionStrategy<UserApiResponse>{

	@Autowired
	private UserService userService;

	private final CheckAdminRule checkAdminRule;
    private final UserStatusRule userStatusRule;
    private final NewUserIdCheckRule newUserIdCheckRule;
    private final UserRoleCheckRule userRoleCheckRule;
    private final UserLocationCheckRule userLocationCheckRule;
	
	private static List<Rule> ruleList = new ArrayList<Rule>();
	
	public InsertUserStrategy(
            CheckAdminRule checkAdminRule, UserStatusRule userStatusRule, NewUserIdCheckRule newUserIdCheckRule, UserRoleCheckRule userRoleCheckRule, UserLocationCheckRule userLocationCheckRule) {
        this.checkAdminRule = checkAdminRule;
        this.userStatusRule = userStatusRule;
        this.newUserIdCheckRule = newUserIdCheckRule;
        this.userRoleCheckRule = userRoleCheckRule;
        this.userLocationCheckRule = userLocationCheckRule;

        this.ruleList = List.of(
                checkAdminRule,
                userStatusRule,
                newUserIdCheckRule,
                userRoleCheckRule,
                userLocationCheckRule
        );
    }
	
	@Override
	public UserApiResponse process(UserActionRequest request, UserActionContext context) {
		ruleList.forEach(r -> r.validate(request, context));
		
		CreateUserRequestDto dto = new CreateUserRequestDto();
		dto.setNewUser(request.getNewUserIds());
		dto.setClientId(request.getClientId());
		dto.setUserId(request.getCurrentUserId().toLowerCase());
		dto.setDealerIds(request.getLocations());
		dto.setRoles(request.getRoles());
		dto.setAction(request.getAction());
		
		CreateUserResponseDto responseDto = userService.createUser(dto);
		
		return new UserApiResponse(HttpStatus.ORDINAL_200_OK, "", "User created successfully", LocalDateTime.now(), responseDto, new ArrayList<>());
	}

	

}
