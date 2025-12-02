package com.space.munova_chat.rsocket.entity;

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
@Table("chat_tag")
public class ChatTag {

    @Id
    @Column("chat_tag_id")
    private Long chatTagId;

    @Column("chat_id")
    private Long chatId;

    @Column("product_category_id")
    private Long productCategoryId;

    @Column("category_type")
    private String categoryType;

    public static ChatTag createChatTag(Long chatId, Long categoryId, String categoryType) {
        return new ChatTag(null, chatId, categoryId, categoryType);
    }
}
