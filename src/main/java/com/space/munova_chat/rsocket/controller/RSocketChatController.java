package com.space.munova_chat.rsocket.controller;

import com.space.munova_chat.rsocket.model.ChatMessage;
import com.space.munova_chat.rsocket.service.RSocketChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RSocketChatController {

    private final RSocketChatService chatService;

    @MessageMapping("chat.send")
    public Mono<Void> send(ChatMessage chatMessage) {
        return chatService.sendMessage(chatMessage);
    }

    @MessageMapping("chat.stream.{chatId}")
    public Flux<ChatMessage> stream(@DestinationVariable Long chatId, @Payload Long memberId, RSocketRequester requester) {
        return chatService.stream(chatId, memberId, requester);
    }

    @MessageMapping("join")
    public Mono<String> join(@Payload ChatMessage msg, RSocketRequester rSocketRequester) {
        log.info("JOIN {}", msg);
        return chatService.join(msg, rSocketRequester)
                .doOnSuccess(v -> log.info("JOIN COMPLETE {}", msg))
                .doOnError(e -> log.error("JOIN ERROR {}", e.getMessage(), e))
                .thenReturn("Join complete");
    }


}
