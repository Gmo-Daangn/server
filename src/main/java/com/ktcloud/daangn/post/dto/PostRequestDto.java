package com.ktcloud.daangn.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PostRequestDto(
        @NotBlank
        String title,
        @NotBlank
        String content,
        @NotNull
        @PositiveOrZero
        Integer price,
        @NotNull
        @Positive
        Long memberId
) {
}
