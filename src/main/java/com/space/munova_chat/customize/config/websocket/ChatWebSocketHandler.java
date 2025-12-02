package com.space.munova_chat.customize.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.space.munova_chat.customize.config.domain.OutGoingMessage;
import com.space.munova_chat.customize.config.session.SessionManager;
import com.space.munova_chat.customize.config.domain.IncomingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final SessionManager sessionManager;
    private final ObjectMapper mapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        log.info("WS CONNECTED sessionId={}", session.getId());

        sessionManager.addSession(session);

        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(payload ->
                        log.debug("WS RECV sessionId={} payload={}", session.getId(), payload)
                )
                .flatMap(payload -> handleMessage(session, payload))
                .doFinally(signal -> {
                    log.info("WS DISCONNECTED sessionId={} reason={}", session.getId(), signal);
                    sessionManager.removeSession(session.getId());
                })
                .then();
    }

    private Mono<Void> handleMessage(WebSocketSession session, String payload) {

        return Mono.fromCallable(() -> mapper.readValue(payload, IncomingMessage.class))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(msg -> {

//                    sessionManager.touchHeartbeat(session.getId());

                    return switch (msg.getType()) {
                        case "JOIN" -> handleJoin(session, msg);
                        case "SEND" -> handleSend(msg);
                        case "UNSUB" -> handleUnsubscribe(session, msg);
                        case "PONG" -> handlePong(session, msg);
                        default -> {
                            log.warn("UNKNOWN messageType={} sessionId={}", msg.getType(), session.getId());
                            yield Mono.empty();
                        }
                    };

                })
                .onErrorResume(e -> {
                    log.error("JSON PARSE ERROR sessionId={} error={}", session.getId(), e.getMessage());
                    return Mono.empty();
                });
    }

    private Mono<Void> handlePong(WebSocketSession session, IncomingMessage msg) {
        sessionManager.touchHeartbeat(session.getId());
        log.info("PONG MESSAGE received sessionId={}", session.getId());
        return Mono.empty();
    }

    // JOIN
    private Mono<Void> handleJoin(WebSocketSession session, IncomingMessage msg) {

        sessionManager.subscribe(session.getId(), msg.getChatId());

        log.info("JOIN sessionId={} chatId={} senderId={}",
                session.getId(), msg.getChatId(), msg.getSenderId());

        OutGoingMessage ack = OutGoingMessage.builder()
                .type("JOIN_ACK")
                .chatId(msg.getChatId())
                .senderId(msg.getSenderId())
                .message("JOIN_OK")
                .timestamp(System.currentTimeMillis())
                .build();

        try {
            String json = mapper.writeValueAsString(ack);
            log.info("json : {}", json);
            return session.send(
                            Flux.just(session.textMessage(json))
                    )
                    .doOnTerminate(() -> log.info("JOIN_ACK sent sessionId={}", session.getId()));
        } catch (Exception e) {
            log.error("JOIN_ACK SEND FAILED sessionId={} error={}", session.getId(), e.getMessage());
            return Mono.empty();
        }
    }

    // SEND
    private Mono<Void> handleSend(IncomingMessage msg) {

        log.info("SEND chatId={} senderId={} message={}",
                msg.getChatId(), msg.getSenderId(), msg.getMessage());

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
                .flatMap(json -> {
                    var receivers = sessionManager.getSessionsForChat(msg.getChatId());
                    log.debug("BROADCAST chatId={} receivers={}",
                            msg.getChatId(), receivers.size());

                    return Flux.fromIterable(receivers)
                            .flatMap(s -> s.send(Mono.just(s.textMessage(json)))
                                    .doOnError(e ->
                                            log.error("SEND FAIL chatId={} sessionId={} error={}",
                                                    msg.getChatId(), s.getId(), e.getMessage()))
                            )
                            .then();
                });
    }

    private Mono<Void> handleUnsubscribe(WebSocketSession session, IncomingMessage msg) {

        log.info("UNSUBSCRIBE sessionId={} chatId={}",
                session.getId(), msg.getChatId());

        sessionManager.unSubscribe(session.getId(), msg.getChatId());
        return Mono.empty();
    }



}