package com.ktcloud.daangn.payment.event;

public record PaymentRequestEvent(
        Long targetMemberId,
        Long amount,
        Long roomId) {
}
