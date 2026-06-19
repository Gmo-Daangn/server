package com.ktcloud.daangn.payment.dto;

import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.payment.entity.PaymentHistory;
import com.ktcloud.daangn.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentRequestDto(
        @NotNull
        UUID tranSeqNo,
        @NotNull
        Long tranAmt,
        @NotNull
        Long memberId
) {
    public PaymentHistory to(Member member, boolean add){
        return PaymentHistory.builder()
                .member(member)
                .tranSeqNo(tranSeqNo)
                .changedCash(tranAmt)
                .type(add ? PaymentStatus.DEPOSIT : PaymentStatus.WITHDRAWAL)
                .balance(member.getBalance())
                .localDateTime(LocalDateTime.now())
                .build();
    }
}
