package com.space.munova_chat.rsocket.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collation = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Message {

    @Id
    private ObjectId id;

    private Long userId;

    private Long chatId;

    private String content;

    private String type;

    private LocalDateTime createdAt;

    public static Message createMessage(String content, String type, Long chatId, Long userId, LocalDateTime createdAt) {
        return new Message(null, userId, chatId, content, type, createdAt);
    }
}
