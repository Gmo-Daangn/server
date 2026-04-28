package com.ktcloud.daangn.post.dto;

import java.time.LocalDateTime;

public record PostResponse(
        Long postId,
        String title,
        String content,
        Integer price,
        String location,
        Integer viewCount,
        LocalDateTime createdAt
) {}