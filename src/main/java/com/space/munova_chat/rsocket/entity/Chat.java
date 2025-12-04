package com.space.munova_chat.rsocket.entity;

import com.space.munova_chat.rsocket.core.BaseEntity;
import com.space.munova_chat.rsocket.dto.GroupChatUpdateRequestDto;
import com.space.munova_chat.rsocket.enums.ChatStatus;
import com.space.munova_chat.rsocket.enums.ChatType;
import com.space.munova_chat.rsocket.exception.ChatException;
import io.r2dbc.spi.Row;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table("chat")
public class Chat extends BaseEntity {

    @Id
    @Column("chat_id")
    private Long id;

    private String name;

    private ChatStatus status;

    private ChatType type;

    @Column("cur_participant")
    private Integer curParticipant;

    @Column("max_participant")
    private Integer maxParticipant;

    @Column("last_message_content")
    private String lastMessageContent;

    @Column("last_message_time")
    private LocalDateTime lastMessageTime;

    @Column("product_id")
    private Long productId;

    public static Chat createChat(String name, ChatStatus status, ChatType type,
                                  Long productId, Integer cur, Integer max) {
        return new Chat(null, name, status, type, cur, max, null, null, productId);
    }

    public static Chat fromRow(Row row) {
        return new Chat(
                row.get("chat_id", Long.class),
                row.get("name", String.class),
                row.get("status", ChatStatus.class),
                row.get("type", ChatType.class),
                row.get("cur_participant", Integer.class),
                row.get("max_participant", Integer.class),
                row.get("last_message_content", String.class),
                row.get("last_message_time", LocalDateTime.class),
                row.get("product_id", Long.class)
        );
    }

    public void modifyLastMessageContent(String content, LocalDateTime time) {
        if (content.length() > 20) {
            this.lastMessageContent = content.substring(0, 20) + "...";
        } else {
            this.lastMessageContent = content;
        }
        this.lastMessageTime = time;
    }

    public void updateInfo(GroupChatUpdateRequestDto dto) {
        if (dto.maxParticipants() < curParticipant) {
            throw ChatException.invalidOperationException("Invalid max participants");
        }
        if (dto.name() != null && !dto.name().isBlank()) {
            this.name = dto.name();
        }
        this.maxParticipant = dto.maxParticipants();
    }

    public void incrementParticipant() {
        if (curParticipant >= maxParticipant) {
            throw ChatException.exceedMaxParticipantsException("Max exceeded");
        }
        this.curParticipant += 1;
    }

    public Chat updateChatStatus(ChatStatus status) {
        if (status == this.status) {
            throw ChatException.invalidChangeException("Chat is already in status: " + this.status);
        }
        this.status = status;
        return this;
    }

    public void decrementParticipant() {
        if (curParticipant <= 0) {
            throw ChatException.cannotDecrementParticipantsException();
        }
        this.curParticipant -= 1;
    }
}
