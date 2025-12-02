package com.space.munova_chat.rsocket.exception;

import com.space.munova_chat.rsocket.core.ResponseApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webflux.autoconfigure.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-2)  // 제일 먼저 처리됨
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    // AbstractErrorWebExceptionHandler 생성자
    public GlobalErrorWebExceptionHandler(GlobalErrorAttributes gxa,
                                          ApplicationContext applicationContext,
                                          ServerCodecConfigurer codecConfigurer) {
        super(gxa, new WebProperties.Resources(), applicationContext);

        super.setMessageWriters(codecConfigurer.getWriters());
        super.setMessageReaders(codecConfigurer.getReaders());
    }


    // Webflux는 routing 방식으로 핸들링 -> 모든 요청에 대해 예외 발생 시 renderErrorResponse 호출
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    // 예외 처리 핸들러
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {

        // context에 저장된 에러 추출
        Throwable error = getError(request);
        log.error("WebFlux Global Error: ", error);

        // 1) BaseException (도메인 에러)
        if (error instanceof BaseException ex) {
            ResponseApi<Object> response = ResponseApi.nok(
                    ex.getStatusCode(),
                    ex.getCode(),
                    combine(ex.getMessage(), ex.getDetailMessage())
            );

            return ServerResponse.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(response);
        }

        // 2) JSON 파싱, 바인딩 오류
        if (error instanceof ServerWebInputException ex) {
            ResponseApi<Object> response = ResponseApi.nok(
                    HttpStatus.BAD_REQUEST,
                    "BAD_REQUEST",
                    ex.getReason()
            );

            return ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(response);
        }

        // 3) 그 외 500
        ResponseApi<Object> response = ResponseApi.nok(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다."
        );

        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response);
    }

    private String combine(String msg, String detail) {
        return detail == null ? msg : msg + " " + detail;
    }

}
