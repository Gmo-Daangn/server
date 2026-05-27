package com.ktcloud.daangn.payment.service;

import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;
import com.ktcloud.daangn.payment.dto.PaymentTradeRequestDto;

public interface PaymentService {

    PaymentResponseDto deposit(PaymentRequestDto dto);

    PaymentResponseDto withdrawal(PaymentRequestDto dto);

    PaymentResponseDto trade(Long fromMemberId,PaymentTradeRequestDto dto);

    String createTrade(Long postId, Long among);
}
