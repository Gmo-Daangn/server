package com.ktcloud.daangn.post.dto;

import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.entity.PostStatus;

import java.time.LocalDateTime;

public record PostDetailResponseDto(
        Long postId,
        Long memberId,
        String sellerNickname,
        String title,
        String content,
        Integer price,
        String location,
        PostStatus status,
        Integer viewCount,
        LocalDateTime createdAt
) {
    public static PostDetailResponseDto from(Post post) {
        return new PostDetailResponseDto(
                post.getId(),
                post.getMemberId(),
                post.getMember().getNickName(),
                post.getTitle(),
                post.getContent(),
                post.getPrice(),
                post.getLocation(),
                post.getStatus(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}
