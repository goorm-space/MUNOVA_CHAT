package com.space.munova_chat.rsocket.entity;

import com.space.munova_chat.rsocket.enums.ChatUserType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table("chat_member")
public class ChatMember {

    @Id
    @Column("chat_member_id")
    private Long id;

    @Column("chat_id")
    private Long chatId;

    @Column("member_id")
    private Long memberId;

    @Column("chat_member_type")
    private ChatUserType chatMemberType;

    private String name;

    public static ChatMember createChatMember(Long chatId, Long memberId,
                                              ChatUserType type, String name) {
        return new ChatMember(null, chatId, memberId, type, name);
    }
}
