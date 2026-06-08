package com.ktcloud.daangn.payment.event;

public record DepositCompleteEvent(
        Long memberId,
        Long amount) {
}
