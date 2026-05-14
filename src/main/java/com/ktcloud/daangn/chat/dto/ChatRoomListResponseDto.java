package com.ktcloud.daangn.chat.dto;

import java.time.LocalDateTime;

public record ChatRoomListResponseDto(
        Long roomId,
        Long productId,
        Long otherMemberId,
        String otherMemberNickname,
        String lastMessage,
        LocalDateTime lastMessageCreatedAt,
        long unreadMessageCount
) {
}
