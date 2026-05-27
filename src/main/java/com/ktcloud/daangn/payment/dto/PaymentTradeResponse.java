package com.ktcloud.daangn.payment.dto;

public record PaymentTradeResponse(
        Long postId,
        String tranSeqNo,
        Long amount
) {
}
