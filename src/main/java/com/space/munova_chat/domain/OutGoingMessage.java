package com.space.munova_chat.domain;

import lombok.Builder;

@Builder
public class OutGoingMessage {
    private String message;
    private Long chatId;
    private Long senderId;
    private String type;
    private long timestamp;
}
