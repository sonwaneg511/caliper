package com.caliper.utils.exception.customException;

public class ResourceNotFoundException extends RuntimeException {
   
	public ResourceNotFoundException(String message) {
        super(message);
    }
}

