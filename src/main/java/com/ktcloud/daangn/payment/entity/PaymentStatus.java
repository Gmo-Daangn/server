package com.ktcloud.daangn.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    DEPOSIT("입금"),
    WITHDRAWAL("출금");

    private final String message;
}
