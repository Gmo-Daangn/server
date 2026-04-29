package com.ktcloud.daangn.chat.dto;

public record ChatRoomEnterRequestDto(
        // 이메일로 임시 사용중
        String memberEmail,
        String targetMemberEmail,
        Long productId
) {
}
