package com.caliper.usermanagement.strategy;

import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;

public interface UserActionStrategy<R> {
	public R process(UserActionRequest request, UserActionContext context);
}
