package com.space.munova_chat.rsocket.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class BaseException extends RuntimeException {

    private final String code;
    private final HttpStatusCode statusCode;
    private final String detailMessage;

    public BaseException(String code, String message, HttpStatusCode statusCode, String... detailMessage) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;

        if (detailMessage != null && detailMessage.length > 0) {
            this.detailMessage = String.join(" ", detailMessage);
        } else {
            this.detailMessage = null;
        }
    }
}
