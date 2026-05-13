package com.ktcloud.daangn.chat.dto;

public record ChatRoomReadResponseDto(
        Long roomId,
        Long memberId,
        Long readMessageCount
) {
}
