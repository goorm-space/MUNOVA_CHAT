package com.space.munova_chat.rsocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.space.munova_chat.rsocket.config.RoomSessionManager;
import com.space.munova_chat.rsocket.model.ChatMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.rabbitmq.Receiver;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConsumerService {

    private final RoomSessionManager roomSessionManager;
    private final Receiver receiver;
    private final ObjectMapper objectMapper;

    @Value("${chat.server.queue.name}")
    private String queueName;

//    @RabbitListener(queues = "#{fanoutQueue.name}")
//    public void receiveMessage(ChatMessage chatMessage) {
//        roomSessionManager.broadcast(chatMessage);
//    }

    @PostConstruct
    public void receiveMessage() {

        receiver.consumeAutoAck(queueName)   // 본인 서버 큐
                .map(delivery -> deserialize(delivery.getBody()))
                .doOnNext(msg -> roomSessionManager.broadcast(msg))
                .doOnError(e -> log.error("MQ Consume Error", e))
                .subscribe();
    }

    private ChatMessage deserialize(byte[] body) {
        try {
            return objectMapper.readValue(body, ChatMessage.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
