package com.caliper.utils.exception.controller;

import java.time.DateTimeException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.caliper.usermanagement.entity.User;
import com.caliper.utils.exception.customException.EmailNotVerifiedException;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.InvalidUserIdsException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.caliper.utils.exception.customException.ReviewNotFoundException;
import com.caliper.utils.exception.customException.UserNotActiveException;
import com.caliper.utils.exception.dto.response.ErrorResponse;
import com.facebook.ads.sdk.APIException;
import com.google.cloud.bigquery.JobException;
import com.mashape.unirest.http.exceptions.UnirestException;

import jakarta.servlet.http.HttpServletRequest;


@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ReviewNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleReviewNotFound(ReviewNotFoundException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.NOT_FOUND.value(),
				"Review Not Found",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(SecurityException.class)
	public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.FORBIDDEN.value(),
				"Not accesible",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(InvalidUserIdsException.class)
	public ResponseEntity<?> handleInvalidUserIds(InvalidUserIdsException ex) {

		return ResponseEntity.badRequest().body(
			    Map.of(
			        "message", "Invalid user IDs",
			        "invalidUserIds", ex.getInvalidDealerIds()
			    )
			);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.BAD_REQUEST.value(),
				"Bad Request",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		StringBuilder errorMessage = new StringBuilder();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errorMessage
			.append(fieldError.getField())
			.append(": ")
			.append(fieldError.getDefaultMessage())
			.append("; ");
		}

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.BAD_REQUEST.value(),
				"Validation Failed",
				errorMessage.toString(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<ErrorResponse> handleInvalidRequest(
			InvalidRequestException ex,
			HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.BAD_REQUEST.value(),
				"Invalid Request Data",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(
			ResourceNotFoundException ex,
			HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.NOT_FOUND.value(),
				"Resource Not Found",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(UserNotActiveException.class)
	public ResponseEntity<ErrorResponse> handleUserNotActive(
			UserNotActiveException ex,
			HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.FORBIDDEN.value(),
				"User Not Active",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(LockedException.class)
	public ResponseEntity<ErrorResponse> handleLocked(LockedException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.LOCKED.value(),
				"Account Locked",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.LOCKED);
	}

	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.FORBIDDEN.value(),
				"Account Disabled",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(EmailNotVerifiedException.class)
	public ResponseEntity<ErrorResponse> handleEmailNotVerified(EmailNotVerifiedException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.FORBIDDEN.value(),
				User.STATUS_PENDING,
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
	}

	// DaoAuthenticationProvider#retrieveUser wraps any non-UsernameNotFoundException thrown from
	// loadUserByUsername (e.g. EmailNotVerifiedException) into an InternalAuthenticationServiceException.
	// Unwrap it here so the specific handler above still applies.
	@ExceptionHandler(InternalAuthenticationServiceException.class)
	public ResponseEntity<ErrorResponse> handleInternalAuthenticationServiceException(
			InternalAuthenticationServiceException ex, HttpServletRequest request) {

		if (ex.getCause() instanceof EmailNotVerifiedException cause) {
			return handleEmailNotVerified(cause, request);
		}

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.UNAUTHORIZED.value(),
				"Authentication Failed",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.BAD_REQUEST.value(),
				"Bad Request",
				"Invalid credential",
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.UNAUTHORIZED.value(),
				"Authentication Failed",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(APIException.class)
	public ResponseEntity<ErrorResponse> handleApiException(APIException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.BAD_GATEWAY.value(),
				"Facebook API Error",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
	}

	@ExceptionHandler(UnirestException.class)
	public ResponseEntity<ErrorResponse> handleUnirestException(UnirestException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.BAD_GATEWAY.value(),
				"External HTTP Request Failed",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
	}

	@ExceptionHandler(SQLException.class)
	public ResponseEntity<ErrorResponse> handleSqlException(SQLException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Database Error",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<ErrorResponse> handleIOException(IOException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"IO Error",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ParseException.class)
	public ResponseEntity<ErrorResponse> handleParseException(ParseException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.BAD_REQUEST.value(),
				"Parse Error",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ErrorResponse> handleNullPointer(
	        NullPointerException ex,
	        HttpServletRequest request) {

	    ErrorResponse error = new ErrorResponse(
	            new Date(),
	            HttpStatus.BAD_REQUEST.value(),
	            "Null Value Error",
	            "Required data is missing",
	            request.getRequestURI()
	    );

	    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(java.time.DateTimeException.class)
	public ResponseEntity<ErrorResponse> handleDateTimeException(
	        DateTimeException ex,
	        HttpServletRequest request) {

	    ErrorResponse error = new ErrorResponse(
	            new Date(),
	            HttpStatus.BAD_REQUEST.value(),
	            "Invalid Date Format",
	            ex.getMessage(),
	            request.getRequestURI()
	    );

	    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
	

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ErrorResponse> handleDatabaseException(
	        DataAccessException ex,
	        HttpServletRequest request) {

	    ErrorResponse error = new ErrorResponse(
	            new Date(),
	            HttpStatus.INTERNAL_SERVER_ERROR.value(),
	            "Database Error",
	            ex.getMostSpecificCause().getMessage(),
	            request.getRequestURI()
	    );

	    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(
	        ConstraintViolationException ex,
	        HttpServletRequest request) {

	    ErrorResponse error = new ErrorResponse(
	            new Date(),
	            HttpStatus.BAD_REQUEST.value(),
	            "Validation Error",
	            ex.getMessage(),
	            request.getRequestURI()
	    );

	    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleInvalidJson(
	        HttpMessageNotReadableException ex,
	        HttpServletRequest request) {

	    ErrorResponse error = new ErrorResponse(
	            new Date(),
	            HttpStatus.BAD_REQUEST.value(),
	            "Malformed JSON Request",
	            "Request body is invalid",
	            request.getRequestURI()
	    );

	    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(DateTimeParseException.class)
	public ResponseEntity<ErrorResponse> handleDateTimeParseException(DateTimeParseException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.BAD_REQUEST.value(),
				"Invalid Date/Time Format",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InterruptedException.class)
	public ResponseEntity<ErrorResponse> handleInterruptedException(InterruptedException ex, HttpServletRequest request) {

		Thread.currentThread().interrupt();
		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Operation Interrupted",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(JobException.class)
	public ResponseEntity<ErrorResponse> handleJobException(JobException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Background Job Error",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				new Date(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal Server Error",
				ex.getMessage(),
				request.getRequestURI()
				);
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
