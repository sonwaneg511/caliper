package com.caliper.usermanagement.rule;

import java.util.List;

import org.springframework.stereotype.Component;

import com.caliper.usermanagement.dto.request.UserActionContext;
import com.caliper.usermanagement.dto.request.UserActionRequest;
import com.caliper.utils.exception.customException.InvalidRequestException;

@Component
public class UserLocationCheckRule implements Rule{

	@Override
	public void validate(UserActionRequest request, UserActionContext context) {
		if (request == null || context == null) {
	        throw new InvalidRequestException("Request or context cannot be null");
	    }
		
		List<String> locations = request.getLocations();
		List<String> currLocations = context.getCurrentUserLocations();
		
		List<String> missingLocations = locations.stream()
		        .filter(loc -> !currLocations.contains(loc))
		        .toList();

		if (locations == null || locations.isEmpty()) {
	        return;
	    }

	    if (currLocations == null || currLocations.isEmpty()) {
	        throw new SecurityException("User does not have any locations");
	    }
	    
	    if (!missingLocations.isEmpty()) {
	        throw new SecurityException("User does not have access to locations: " + missingLocations);
	    }
	}

}
