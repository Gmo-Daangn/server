package com.ktcloud.daangn.payment.dto;

import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.payment.entity.PaymentHistory;
import com.ktcloud.daangn.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentRequestDto(
        @NotBlank
        UUID tran_seq_no,
        @NotBlank
        Long tran_amt,
        @NotBlank
        Long memberId
) {
    public PaymentHistory to(Member member, boolean add){
        return PaymentHistory.builder()
                .member(member)
                .tranSeqNo(tran_seq_no)
                .changedCash(tran_amt)
                .type(add ? PaymentStatus.DEPOSIT : PaymentStatus.WITHDRAWAL)
                .balance(member.getBalance())
                .localDateTime(LocalDateTime.now())
                .build();
    }
}
