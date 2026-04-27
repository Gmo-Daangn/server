package com.ktcloud.daangn.chat.dto;

public record ChatMessageRequestDto(
        //Long memberId,
        //id검색 구현 전 임시 사용
        String memberEmail,
        Long roomId,
        String message
) {
}
