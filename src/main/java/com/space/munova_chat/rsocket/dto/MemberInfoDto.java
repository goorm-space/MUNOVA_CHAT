package com.space.munova_chat.rsocket.dto;

import com.space.munova_chat.rsocket.entity.ChatMember;
import com.space.munova_chat.rsocket.enums.ChatUserType;

public record MemberInfoDto(
        Long memberId,
        String name,
        ChatUserType chatUserType

) {
    public static MemberInfoDto of(Long memberId, String name, ChatUserType chatUserType) {
        return new MemberInfoDto(memberId, name, chatUserType);
    }

    public static MemberInfoDto of(ChatMember cm) {
        return new MemberInfoDto(cm.getMemberId(), cm.getName(), cm.getChatMemberType());
    }
}

