package com.caliper.usermanagement.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;
import com.caliper.usermanagement.entity.User;
import com.caliper.usermanagement.repository.UserRepository;
import com.caliper.utils.exception.customException.InvalidUserIdsException;

@Component
public class NewUserIdCheckRule implements Rule{

	@Autowired
	public UserRepository userRepository;
	
	@Override
	public void validate(UserActionRequest request, UserActionContext context) {
		List<String> newUserIdList = request.getNewUserIds();
		
		List<String> invalidUserIdList = userRepository
		        .findByUserIdIn(newUserIdList)
		        .stream()
		        .map(User::getUserId)
		        .toList();

		if (!invalidUserIdList.isEmpty()) {
		    throw new InvalidUserIdsException(invalidUserIdList);
		}

	}
}
