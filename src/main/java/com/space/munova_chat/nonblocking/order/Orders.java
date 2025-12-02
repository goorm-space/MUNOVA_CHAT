package com.space.munova_chat.nonblocking.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Getter
@RestController
@NoArgsConstructor
@Table("orders")
public class Orders {
    @Id
    private Long id;

    private String orderNum;    // 주문 번호

    private String description; // 주문 입력 비고

    public Orders(String orderNum, String description) {
        this.orderNum = orderNum;
        this.description = description;
    }
}
