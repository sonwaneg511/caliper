package com.caliper.utils.exception.dto.response;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {

	private Date timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

}
