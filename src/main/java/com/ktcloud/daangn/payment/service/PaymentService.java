package com.ktcloud.daangn.payment.service;

import com.ktcloud.daangn.payment.dto.PaymentCreatTradeRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;

public interface PaymentService {

    PaymentResponseDto deposit(PaymentRequestDto dto);

    PaymentResponseDto withdrawal(PaymentRequestDto dto);

    PaymentResponseDto trade(Long fromMemberId, String tranSeqNo, Long among, Long postId);

    String createTrade(PaymentCreatTradeRequestDto dto);
}
