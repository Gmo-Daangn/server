package com.ktcloud.daangn.payment.dto;

public record PaymentInitRequestDto(
        Long roomId,
        Long postId,
        Long amount
) {
}
