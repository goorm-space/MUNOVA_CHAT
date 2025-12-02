package com.space.munova_chat.rsocket.repository;

import com.space.munova_chat.rsocket.entity.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRepositoryCustomImpl implements ChatRepositoryCustom {

    private final DatabaseClient databaseClient;

    @Override
    public Flux<Chat> searchGroupChats(String keyword, List<Long> tagIds, Long memberId, boolean isMine) {
        StringBuilder sql = new StringBuilder("""
                    SELECT DISTINCT c.* FROM chat c,
                """);

        if (tagIds != null || !tagIds.isEmpty()) {
            sql.append("""
                        JOIN chat_tag ct
                        ON ct.chat_id = c.chat_id
                    """);
        }

        if (isMine) {
            sql.append("""
                        JOIN chat_member cm
                        ON cm.chat_id = c.chat_id
                    """);
        }

        sql.append("""
                    WHERE c.type = 'GROUP'
                """);

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("""
                    AND LOWER(c.name) LIKE LOWER(:keyword)
                    """);
        }

        // tagIds
        if (tagIds != null && !tagIds.isEmpty()) {
            sql.append(" AND ct.product_category_id IN (:tagIds) ");
        }

        // isMine
        if (isMine) {
            sql.append(" AND cm.member_id = :memberId ");
        }

        return databaseClient.sql(sql.toString())
                .bind("keyword", keyword == null ? null : "%" + keyword.toLowerCase() + "%")
                .bind("tagsId", tagIds == null ? List.of() : tagIds)
                .bind("memberId", memberId)
                .map((row, meta) -> Chat.fromRow(row))
                .all();

    }
}
