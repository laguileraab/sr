package com.ce.sr.payload.response;

import org.springframework.http.HttpStatus;

public class MessageResponse {
	private String message;

	public MessageResponse(HttpStatus conflict, String message) {
	    this.message = message;
	  }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
