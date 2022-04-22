package com.ce.sr.exceptions;

import com.ce.sr.payload.response.MessageResponseError;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<MessageResponseError> resourceNotFoundException(ResourceNotFoundException ex,
			WebRequest request) {
		MessageResponseError errorDetails = new MessageResponseError(HttpStatus.NOT_FOUND, ex.getMessage(),
				"ResourceNotFound_" + request.getDescription(false), ExceptionUtils.getStackTrace(ex));
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(FileForbiddenException.class)
	public ResponseEntity<MessageResponseError> fileForbiddenException(FileForbiddenException ex,
			WebRequest request) {
		MessageResponseError errorDetails = new MessageResponseError(HttpStatus.FORBIDDEN, ex.getMessage(),
				"Forbidden_" + request.getDescription(false), ExceptionUtils.getStackTrace(ex));
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<MessageResponseError> globalExceptionsHandler(Exception ex, WebRequest request) {
		MessageResponseError errorDetails = new MessageResponseError(HttpStatus.INTERNAL_SERVER_ERROR,
				"Oops! There's been an error", "Error_in_" + request.getDescription(false), ExceptionUtils.getStackTrace(ex));
		return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}