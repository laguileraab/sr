package com.ce.sr.payload.response;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseError {
    private HttpStatus httpStatus;
    private String message;
    private String code;
    private String backEndMessage;
}
