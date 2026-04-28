package com.ktcloud.daangn.post.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer price;
    private String location;
    private String status;
    private Integer viewCount;
    private LocalDateTime createdAt;

    public Post(Long memberId, String title, String content, Integer price, String location) {
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.price = price;
        this.location = location;
        this.status = "판매중";
        this.viewCount = 0;
        this.createdAt = LocalDateTime.now();
    }
}