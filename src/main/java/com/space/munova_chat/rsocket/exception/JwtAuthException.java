package com.space.munova_chat.rsocket.exception;

import org.springframework.http.HttpStatus;

public class JwtAuthException extends BaseException {

    public JwtAuthException(String code, String message) {
        super(code, message, HttpStatus.UNAUTHORIZED);
    }

    public static JwtAuthException invalidSignature() {
        return new JwtAuthException("AUTH_ERROR_01", "유효하지 않은 JWT 서명입니다.");
    }

    public static JwtAuthException expired() {
        return new JwtAuthException("AUTH_ERROR_02", "만료된 JWT 토큰입니다.");
    }

    public static JwtAuthException unsupported() {
        return new JwtAuthException("AUTH_ERROR_03", "지원하지 않는 JWT 토큰입니다.");
    }

    public static JwtAuthException illegal() {
        return new JwtAuthException("AUTH_ERROR_04", "잘못된 JWT 토큰입니다.");
    }

}
