package com.space.munova_chat.rsocket.controller;

import com.space.munova_chat.rsocket.config.RoomSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RSocketConnectHandler {

    // 사용자 별 RSocketRequester 등록 -> 한 사용자가 복수의 세션을 가질 수 있음 (멀티 디바이스 가능) -> 이거에 대한 제한은? 몇 개씩 다 허용해줘도 되나..?
    private final RoomSessionManager sessionManager;

    @ConnectMapping
    public Mono<Void> onConnect(RSocketRequester requester) {
        sessionManager.onConnect(requester);
        log.info("🟢 RSocket CONNECT 성공! requester={}", requester);
        return Mono.empty();
    }

}
