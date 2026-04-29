package com.ktcloud.daangn.chat.dto;

import java.time.LocalDateTime;

public record ChatMessageResponseDto(
        Long messageId,
        Long roomId,
        String senderEmail,
        String message,
        boolean edited,
        boolean deleted,
        long unreadCount,
        LocalDateTime createdAt
) {
}
