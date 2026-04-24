package com.ktcloud.daangn.post.ntt;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    private Long memberId;
    private String title;
    private String content;
    private Integer price;
    private String location;
    private String status;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private String thumbnailUrl;

    public Post() {
    }

    public Post(Long memberId, String title, String content, Integer price, String location, String thumbnailUrl) {
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.price = price;
        this.location = location;
        this.thumbnailUrl = thumbnailUrl;
        this.status = "판매중";
        this.viewCount = 0;
        this.likeCount = 0;
        this.createdAt = LocalDateTime.now();
    }
}