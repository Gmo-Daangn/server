package com.ktcloud.daangn.post.dto;

import java.time.LocalDateTime;

public record PostListResponse(
        Long postId,
        String title,
        Integer price,
        String location,
        Integer viewCount,
        LocalDateTime createdAt
) {}