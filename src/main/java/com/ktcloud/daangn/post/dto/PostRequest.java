package com.ktcloud.daangn.post.dto;

public record PostRequest(
        String title,
        String content,
        Integer price,
        Long memberId
) {}