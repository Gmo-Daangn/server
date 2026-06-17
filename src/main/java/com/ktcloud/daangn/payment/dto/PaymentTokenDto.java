package com.ktcloud.daangn.payment.dto;

import com.ktcloud.daangn.payment.entity.PaymentToken;

import java.util.UUID;

public record PaymentTokenDto(
        UUID tranSeqNo,
        Long amount,
        Long postId
) {
    public static PaymentTokenDto from(PaymentToken token) {
        return new PaymentTokenDto(token.tranSeqNo, token.amount, token.postId);
    }
}
