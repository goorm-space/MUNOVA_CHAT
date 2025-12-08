package com.space.munova_chat.rsocket.service;

import com.space.munova_chat.rsocket.config.RoomSessionManager;
import com.space.munova_chat.rsocket.entity.Message;
import com.space.munova_chat.rsocket.model.ChatMessage;
import com.space.munova_chat.rsocket.repository.mongodb.MessageMongoRepository;
import com.space.munova_chat.rsocket.repository.r2dbc.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RSocketChatService {

    private final RoomSessionManager sessionManager;
    private final MessageRepository messageRepository;
    private final MessageMongoRepository messageMongoRepository;

    // 채팅방 별 메시지 Sink
    private final Map<Long, Sinks.Many<ChatMessage>> roomSinks = new ConcurrentHashMap<>();

    // 채팅방 입장
    public Mono<String> join(ChatMessage msg, RSocketRequester rsocketRequester) {

        // 권한 체크 들어갈 수 있음
        log.info("JOIN 요청: chatId={}, senderId={}", msg.getChatId(), msg.getSenderId());

        sessionManager.joinChat(msg.getChatId(), msg.getSenderId(), rsocketRequester);

        // 채팅방에 누가 들어왔음을 알리는 메시지
        getSink(msg.getChatId()).tryEmitNext(msg);

        return Mono.just("JOIN_OK");
    }

    // 해당 채팅방 SINK로 메시지 전송
    public Mono<Void> sendMessage(ChatMessage msg) {
        log.info("SEND {}", msg.toString());

        Message message = Message.createMessage(msg.getContent(), msg.getType(), msg.getChatId(), msg.getSenderId());

        // Flux로 구독중인 사용자들에게 메시지 브로드캐스트
//        return messageRepository.save(message)
        return messageMongoRepository.save(message)
                        .doOnNext(saved -> {
                                ChatMessage out = new ChatMessage(saved.getType(), saved.getChatId(), saved.getUserId(), saved.getContent(), saved.getCreatedAt().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());
                                getSink(saved.getChatId()).tryEmitNext(msg);
                        })
                        .then();
    }

    // STREAM 구독(메시지 전송 출구)
    public Flux<ChatMessage> stream(Long chatId) {
        log.info("➡️ Stream requested for room {}", chatId);
        return getSink(chatId).asFlux();
    }

    // 특정 채팅방의 SINK 얻기 (메시지 입력 입구)
    private Sinks.Many<ChatMessage> getSink(Long chatId) {
        return roomSinks.computeIfAbsent(
                chatId, id -> {
                    log.info("➡️ Create new Sink for room {}", id);
                    return Sinks.many().multicast().onBackpressureBuffer();
                }
        );
    }

}
