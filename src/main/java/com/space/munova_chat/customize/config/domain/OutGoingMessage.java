package com.space.munova_chat.customize.config.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OutGoingMessage {
    private String message;
    private Long chatId;
    private Long senderId;
    private String type;
    private long timestamp;
}
