package com.ktcloud.daangn.post.dto;

public record PostCreateResponse(
        Long postId,
        Long memberId,
        String message
) {}