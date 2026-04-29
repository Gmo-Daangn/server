package com.ktcloud.daangn.post.dto;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long postId,
        String sellerNickname,
        String title,
        String content,
        Integer price,
        String location,
        String status,
        Integer viewCount,
        LocalDateTime createdAt
) {}