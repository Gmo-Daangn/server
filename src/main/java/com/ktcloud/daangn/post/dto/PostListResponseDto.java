package com.ktcloud.daangn.post.dto;

import com.ktcloud.daangn.post.entity.Post;

import java.time.LocalDateTime;

public record PostListResponseDto(
        Long postId,
        Long memberId,
        String title,
        Integer price,
        String location,
        Integer viewCount,
        LocalDateTime createdAt
) {
    public static PostListResponseDto from(Post post) {
        return new PostListResponseDto(
                post.getId(),
                post.getMemberId(),
                post.getTitle(),
                post.getPrice(),
                post.getLocation(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}
