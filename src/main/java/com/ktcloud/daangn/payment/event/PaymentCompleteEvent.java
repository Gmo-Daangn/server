package com.ktcloud.daangn.payment.event;

public record PaymentCompleteEvent(
        Long sellerId,
        Long amount,
        Long roomId) {
}
