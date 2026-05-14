package com.ktcloud.daangn.post.entity;

import com.ktcloud.daangn.member.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* TODO: integer 자료형 관련된 수정 고민 및 location에 대한 리펙토링 필요
 */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer price;
    private String location;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    private Integer viewCount;
    private LocalDateTime createdAt;

    @Builder
    public Post(Member member, String title, String content, Integer price, String location) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.price = price;
        this.location = location;
        this.status = PostStatus.FOR_SALE;
        this.viewCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public static Post create(Member member, String title, String content, Integer price, String location) {
        return Post.builder()
                .member(member)
                .title(title)
                .content(content)
                .price(price)
                .location(location)
                .build();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void update(String title, String content, Integer price, PostStatus status) {
        this.title = title;
        this.content = content;
        this.price = price;
        if (status != null) {
            this.status = status;
        }
    }

    public boolean isOwner(Long memberId) {
        return memberId != null && member != null && memberId.equals(member.getId());
    }

    public Long getMemberId() {
        return member.getId();
    }
}
