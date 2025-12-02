package com.space.munova_chat.rsocket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ProductException extends BaseException {

    public ProductException(String code, String message, HttpStatusCode statusCode, String... detailMessage) {
        super(code, message, statusCode, detailMessage);
    }

    public static ProductException notFoundProductException(String... detailMessage) {
        return new ProductException("PRODUCT_01", "유효하지 않은 상품입니다.", HttpStatus.NOT_FOUND, detailMessage);
    }

}
