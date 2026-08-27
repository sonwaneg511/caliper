package com.caliper.usermanagement.rule;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.caliper.usermanagement.entity.UserOperationEnum;
import com.caliper.usermanagement.strategy.DeleteUserStrategy;
import com.caliper.usermanagement.strategy.EditUserStrategy;
import com.caliper.usermanagement.strategy.GetUserStrategy;
import com.caliper.usermanagement.strategy.InsertUserStrategy;
import com.caliper.usermanagement.strategy.UserActionStrategy;

@Component
public class UserActionStrategyFactory {
	private Map<UserOperationEnum, UserActionStrategy<?>> strategies = new EnumMap<>(UserOperationEnum.class);

	public UserActionStrategyFactory(InsertUserStrategy insertStrategy, GetUserStrategy getStrategy,
			EditUserStrategy editStrategy, DeleteUserStrategy deleteStrategy) {
		strategies.put(UserOperationEnum.INSERT, insertStrategy);
		strategies.put(UserOperationEnum.GET, getStrategy);
		strategies.put(UserOperationEnum.EDIT, editStrategy);
		strategies.put(UserOperationEnum.DELETE, deleteStrategy);
	}

	@SuppressWarnings("unchecked")
	public  <R> UserActionStrategy<R> getStrategy (UserOperationEnum action){
	       UserActionStrategy<?> strategy = strategies.get(action);

	        if (strategy == null) {
	            throw new IllegalArgumentException(
	                "No strategy found for action: " + action
	            );
	        }

	        return (UserActionStrategy<R>) strategy;
	}
}
