package com.ktcloud.daangn.payment.dto;

import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.payment.entity.PaymentHistory;

import java.time.LocalDateTime;

public record PaymentRequestDto(
        String tran_seq_no,
        Long tran_amt,
        Long memberId
) {
    public PaymentHistory to(Member member, boolean add){
        return PaymentHistory.builder()
                .member(member)
                .tranSeqNo(tran_seq_no)
                .changedCash(tran_amt)
                .type(add ? "입금" : "출금")
                .balance(member.getBalance())
                .localDateTime(LocalDateTime.now())
                .build();
    }
}
