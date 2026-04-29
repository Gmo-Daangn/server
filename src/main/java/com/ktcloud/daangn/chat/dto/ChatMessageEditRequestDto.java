package com.ktcloud.daangn.chat.dto;

public record ChatMessageEditRequestDto(
        String memberEmail,
        String message
) {
}
