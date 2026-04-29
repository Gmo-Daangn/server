package com.ktcloud.daangn.chat.dto;

public record ChatRoomReadResponseDto(
        Long roomId,
        String memberEmail,
        int readMessageCount
) {
}
