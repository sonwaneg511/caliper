package com.caliper.usermanagement.rule;

import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;

public interface Rule {
	public void validate(UserActionRequest request, UserActionContext context);
}
