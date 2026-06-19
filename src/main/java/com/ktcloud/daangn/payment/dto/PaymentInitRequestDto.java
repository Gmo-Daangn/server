package com.ktcloud.daangn.payment.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentInitRequestDto(
        @NotNull
        Long roomId,
        @NotNull
        Long postId,
        @NotNull
        Long amount
) {
}
