package com.space.munova_chat.rsocket.repository.r2dbc;

import com.space.munova_chat.rsocket.entity.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ChatRepositoryCustomImpl implements ChatRepositoryCustom {

    private final DatabaseClient databaseClient;

    @Override
    public Flux<Chat> searchGroupChats(String keyword, List<Long> tagIds, Long memberId, boolean isMine) {

        boolean hasTags = tagIds != null && !tagIds.isEmpty();
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        StringBuilder sql = new StringBuilder("""
                  SELECT DISTINCT c.*
                  FROM chat c
                """);

        if (hasTags) {
            sql.append(" JOIN chat_tag ct ON ct.chat_id = c.chat_id ");
        }

        if (isMine) {
            sql.append(" JOIN chat_member cm ON cm.chat_id = c.chat_id ");
        }

        sql.append(" WHERE c.type = 'GROUP' ");

        if (hasKeyword) {
            sql.append(" AND LOWER(c.name) LIKE '%")
                    .append(keyword.toLowerCase())
                    .append("%' ");
        }

        // IN 절은 문자열로 직접 삽입
        if (hasTags) {
            String ids = tagIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            sql.append(" AND ct.product_category_id IN (" + ids + ") ");
        }

        if (isMine) {
            sql.append(" AND cm.member_id = ").append(memberId);
        }

        return databaseClient.sql(sql.toString())
                .map((row, meta) -> Chat.fromRow(row))
                .all();
    }
}
