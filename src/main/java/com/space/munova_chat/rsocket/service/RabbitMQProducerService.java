package com.space.munova_chat.rsocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.space.munova_chat.rsocket.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQProducerService {

    private final Sender rabbitSender;
    //    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    @Value("${chat.server.exchange.name}")
    private String exchangeName;

    public Mono<Void> sendMessage(ChatMessage chatMessage) {
//        rabbitTemplate.convertAndSend(FANOUT_NAME, "", chatMessage);

        return Mono.fromCallable(() ->
                        objectMapper.writeValueAsBytes(chatMessage)  // 블로킹 호출 -> fromCallable
                )
                .map(bytes ->
                        new OutboundMessage(exchangeName, "", bytes)
                )
                .as(rabbitSender::send)
                .doOnError(e -> log.error("❌ MQ publish failed", e))
                .then();
    }
}
