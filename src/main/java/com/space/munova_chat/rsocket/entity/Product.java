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
@Table("product")
public class Product {

    @Id
    @Column("product_id")
    private Long id;

    private String info;

    private String name;

    private Long price;

    @Column("like_count")
    private Integer likeCount = 0;

    @Column("sales_count")
    private Integer salesCount = 0;

    @Column("view_count")
    private Integer viewCount = 0;

    @Column("brand_id")
    private Long brandId;

    @Column("product_category_id")
    private Long categoryId;

    @Column("member_id")
    private Long memberId;

    @Column("is_deleted")
    private Boolean isDeleted;

}
