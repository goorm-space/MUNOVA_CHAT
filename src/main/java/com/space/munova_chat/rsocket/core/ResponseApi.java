package com.space.munova_chat.rsocket.core;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseApi<T> {

    private int statusCode;
    private String code;
    private String message;
    private T data;

    private ResponseApi(int statusCode, T data) {
        this.statusCode = statusCode;
        this.data = data;
        this.code = "success";
        this.message = "요청에 성공했습니다";
    }

    private ResponseApi(int statusCode, String code, String message, T data) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 200 OK (data 없음)
    public static <T> ResponseApi<T> ok() {
        return new ResponseApi<>(HttpStatus.OK.value(), null);
    }

    // 200 OK (data 있음)
    public static <T> ResponseApi<T> ok(T data) {
        return new ResponseApi<>(HttpStatus.OK.value(), data);
    }

    // 201 Created — WebFlux 스타일 (Servlet Response 제거)
    public static <T> ResponseApi<T> created(T data) {
        return new ResponseApi<>(HttpStatus.CREATED.value(), data);
    }

    // Error 응답
    public static <T> ResponseApi<T> nok(HttpStatusCode statusCode, String errorCode, String message) {
        return new ResponseApi<>(statusCode.value(), errorCode, message, null);
    }
}