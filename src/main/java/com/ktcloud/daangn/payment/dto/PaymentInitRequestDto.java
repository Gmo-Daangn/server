package com.ktcloud.daangn.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentInitRequestDto(
        @NotBlank
        Long roomId,
        @NotBlank
        Long postId,
        @NotBlank
        Long amount
) {
}
