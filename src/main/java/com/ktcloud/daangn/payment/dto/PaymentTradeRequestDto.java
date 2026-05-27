package com.ktcloud.daangn.payment.dto;

public record PaymentTradeRequestDto(
        Long postId,
        String tranSeqNo,
        Long among
) {
}
