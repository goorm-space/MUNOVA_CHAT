package com.space.munova_chat.websocket;

import com.space.munova_chat.domain.OutGoingMessage;
import com.space.munova_chat.session.SessionManager;
import com.space.munova_chat.domain.IncomingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final SessionManager sessionManager;
    private final ObjectMapper mapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        // 웹소켓 연결 시 session 등록
        sessionManager.addSession(session);

        // 웹소켓 연결이 유지되는 동안 클라이언트가 보내는 메시지가 하나씩 FLUX로 흘러들어옴
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)    // JSON 문자열 반환
                .flatMap(payload -> handleMessage(session, payload))    // 라우팅으로 넘김
                .doFinally(signal -> sessionManager.removeSession(session.getId()))    // 연결 종료 시 cleanup
                .then();
    }

    private Mono<Void> handleMessage(WebSocketSession session, String payload) {

        // JSON 파싱 -> IncomingMessage로 변환
        // Non-EventLoop에서 실행해라
        return Mono.fromCallable(() -> mapper.readValue(payload, IncomingMessage.class))
                .subscribeOn(Schedulers.boundedElastic())    // boundedElastic 스레드풀에서 실행해라
                .flatMap(msg -> {

                    // heartbeat 갱신
                    sessionManager.touchHeartbeat(session.getId());

                    // message 종류 검사
                    return switch (msg.getType()) {
                        case "JOIN" -> handleJoin(session, msg);
                        case "SEND" -> handleSend(msg);
                        case "UNSUB" -> handleUnsubscribe(session, msg);
                        default -> Mono.empty();
                    };

                })
                .onErrorResume(e -> {
                    log.error("JSON parsing error: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    // JOIN
    private Mono<Void> handleJoin(WebSocketSession session, IncomingMessage msg) {
        sessionManager.subscribe(session.getId(), msg.getChatId());
        return Mono.empty();
    }

    // SEND
    private Mono<Void> handleSend(IncomingMessage msg) {

        return Mono.fromCallable(() -> mapper.writeValueAsString(
                        OutGoingMessage.builder()
                                .type("MESSAGE")
                                .chatId(msg.getChatId())
                                .senderId(msg.getSenderId())
                                .message(msg.getMessage())
                                .timestamp(System.currentTimeMillis())
                                .build()
                ))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(json ->
                        Flux.fromIterable(sessionManager.getSessionsForChat(msg.getChatId()))
                                .flatMap(s -> s.send(Mono.just(s.textMessage(json))).onErrorResume(e -> Mono.empty()))
                                .then()
                );
    }

    private Mono<Void> handleUnsubscribe(WebSocketSession session, IncomingMessage msg) {
        sessionManager.unSubscribe(session.getId(), msg.getChatId());
        return Mono.empty();
    }
}
