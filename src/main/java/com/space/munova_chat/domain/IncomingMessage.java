package com.space.munova_chat.domain;

import lombok.Data;

@Data
public class IncomingMessage {
    private String message;
    private Long chatId;
    private Long senderId;
    private String type;    // JOIN, SEND, UNSUB
}
