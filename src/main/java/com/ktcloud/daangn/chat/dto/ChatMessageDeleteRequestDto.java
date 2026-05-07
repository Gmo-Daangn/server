package com.ktcloud.daangn.chat.dto;

import jakarta.validation.constraints.NotNull;

public record ChatMessageDeleteRequestDto(
        @NotNull(message = "회원 ID는 비어 있을 수 없습니다.")
        Long memberId
) {
}
