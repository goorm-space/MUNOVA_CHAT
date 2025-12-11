package com.space.munova_chat.rsocket.service;

import com.space.munova_chat.rsocket.config.RoomSessionManager;
import com.space.munova_chat.rsocket.entity.Message;
import com.space.munova_chat.rsocket.model.ChatMessage;
import com.space.munova_chat.rsocket.repository.mongodb.MessageMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RSocketChatService {

    private final RoomSessionManager sessionManager;
    private final MessageMongoRepository messageMongoRepository;
    private final RabbitMQProducerService producerService;

    ///  채팅방 JOIN
    public Mono<String> join(ChatMessage msg, RSocketRequester rsocketRequester) {

        sessionManager.joinChat(msg.getChatId(), msg.getSenderId(), rsocketRequester);

        return Mono.just("JOIN_OK");
    }

    /// 채팅방 SINK로 메시지 전송
    public Mono<Void> sendMessage(ChatMessage msg) {

        // 1) MQ 브로드캐스트 → 별도 쓰레드에서 실행 (절대 EventLoop 차단 X)
        producerService.sendMessage(msg)
                .subscribe();

        messageMongoRepository.save(
                        Message.createMessage(msg.getContent(), msg.getType(), msg.getChatId(), msg.getSenderId(), msg.getCreatedAt())
                )
                .subscribe();

        return Mono.empty();
    }

    ///  STREAM 구독
    public Flux<ChatMessage> stream(Long chatId, Long memberId, RSocketRequester requester) {
        Sinks.Many<ChatMessage> sink = sessionManager.getOrCreateSink(chatId, requester);
        ChatMessage joinEvent = new ChatMessage(
                "JOIN_EVENT",
                chatId,
                memberId,
                memberId + "님이 입장했습니다.",
                System.currentTimeMillis(),
                LocalDateTime.now()
        );

        sink.tryEmitNext(joinEvent);

        return sink.asFlux();
    }
}
