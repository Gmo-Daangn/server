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
    private String title;    // 제목

    @Column(columnDefinition = "TEXT")
    private String content;  // 내용

    private Integer price;   // 가격
    private String location; // 지역
    private String status;   // 상태
    private Integer viewCount; // 조회수
    private String thumbnailUrl; // 이미지 주소
    private LocalDateTime createdAt; // 작성 시간

    // 생성자
    public Post(Long memberId, String title, String content, Integer price, String location, String thumbnailUrl) {
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.price = price;
        this.location = location;
        this.thumbnailUrl = thumbnailUrl;
        this.status = "판매중";
        this.viewCount = 0;
        this.createdAt = LocalDateTime.now();
    }
}