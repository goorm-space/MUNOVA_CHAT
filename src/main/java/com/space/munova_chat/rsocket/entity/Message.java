package com.space.munova_chat.rsocket.entity;

import com.space.munova_chat.rsocket.core.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Document(collation = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table("message")
public class Message extends BaseEntity {

    @Id
    @Column("message_id")
    private ObjectId id;

    @Column("user_id")
    private Long userId;

    @Column("chat_id")
    private Long chatId;

    private String content;

    private String type;


    public static Message createMessage(String content, String type, Long chatId, Long userId) {
        return new Message(null, userId, chatId, content, type);
    }
}
