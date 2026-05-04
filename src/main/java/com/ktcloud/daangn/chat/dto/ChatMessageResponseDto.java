package com.ktcloud.daangn.chat.dto;

import com.ktcloud.daangn.chat.entity.ChatMessage;

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
    public static ChatMessageResponseDto from(ChatMessage chatMessage) {
        return new ChatMessageResponseDto(
                chatMessage.getId(),
                chatMessage.getChatRoom().getId(),
                chatMessage.getMember().getEmail(),
                chatMessage.getMessage(),
                chatMessage.isEdited(),
                chatMessage.isDeleted(),
                chatMessage.getReadCount(),
                chatMessage.getCreatedAt()
        );
    }
}
