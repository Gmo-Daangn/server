package com.ktcloud.daangn.chat.dto;

public record ChatRoomEnterResponseDto(
        Long roomId,
        boolean created,
        String message
) {
}
