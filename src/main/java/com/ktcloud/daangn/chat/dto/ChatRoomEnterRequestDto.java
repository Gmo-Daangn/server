package com.ktcloud.daangn.chat.dto;

public record ChatRoomEnterRequestDto(
        //Long memberId,
        //Long targetMemberId,
        // 아이디 검색 구현 전 까지 임시로 사용
        String memberEmail,
        String targetMemberEmail,
        Long productId,
        Long roomId
) {
}
