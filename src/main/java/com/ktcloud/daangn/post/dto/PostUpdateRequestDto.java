package com.ktcloud.daangn.post.dto;

import com.ktcloud.daangn.post.entity.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PostUpdateRequestDto(
        @NotBlank
        String title,
        @NotBlank
        String content,
        @NotNull
        @PositiveOrZero
        Integer price,
        PostStatus status,
        @NotNull
        @Positive
        Long memberId
) {
}
