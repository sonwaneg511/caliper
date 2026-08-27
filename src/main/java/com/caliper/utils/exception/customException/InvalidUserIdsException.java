package com.caliper.utils.exception.customException;

import java.util.List;

public class InvalidUserIdsException extends RuntimeException{

	private List<String> invalidDealerIds;

	public InvalidUserIdsException(List<String> invalidDealerIds) {
        this.invalidDealerIds = invalidDealerIds;
    }
	
	public List<String> getInvalidDealerIds() {
        return invalidDealerIds;
    }
	
}
