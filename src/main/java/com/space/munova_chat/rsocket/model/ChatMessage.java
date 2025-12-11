package com.space.munova_chat.rsocket.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String type;    // JOIN, UNSUB, SEND,
    private Long chatId;
    private Long senderId;
    private String content;
    private long timestamp;
    private LocalDateTime createdAt;
}