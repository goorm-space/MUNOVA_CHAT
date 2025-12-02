package com.space.munova_chat.customize.config.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncomingMessage {
    private String message;
    private Long chatId;
    private Long senderId;
    private String type;    // JOIN, SEND, UNSUB
}
