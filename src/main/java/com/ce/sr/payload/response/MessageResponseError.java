package com.ce.sr.payload.response;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageResponseError {
    private HttpStatus httpStatus;
    private String message;
    private String code;
    private String backEndMessage;
}
