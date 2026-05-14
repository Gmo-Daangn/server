package com.ktcloud.daangn.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatMessageWriteRequestDto(
        @NotNull(message = "회원 ID는 비어 있을 수 없습니다.")
        Long memberId,

        @NotBlank(message = "메시지는 비어 있을 수 없습니다.")
        String message
) {
}
