package com.caliper.usermanagement.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;
import com.caliper.usermanagement.entity.User;
import com.caliper.usermanagement.repository.UserRepository;
import com.caliper.utils.exception.customException.UserNotActiveException;

@Component
public class UserStatusRule implements Rule{

	@Autowired
	public UserRepository userRepository;

	@Override
	public void validate(UserActionRequest request, UserActionContext context) {

	    User user = context.getCurrentUser();
	    User targetUser = context.getTargetUserObj();

	    if (user != null && !User.STATUS_ACTIVE.equalsIgnoreCase(user.getActive())) {
	        throw new UserNotActiveException("User is not active");
	    }

	    if (targetUser != null && !User.STATUS_ACTIVE.equalsIgnoreCase(targetUser.getActive())) {
	        throw new UserNotActiveException("Target user is not active");
	    }
	}


}
