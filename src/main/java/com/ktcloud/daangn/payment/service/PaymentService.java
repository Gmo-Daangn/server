package com.ktcloud.daangn.payment.service;

import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;

public interface PaymentService {

    PaymentResponseDto deposit(PaymentRequestDto dto);

    PaymentResponseDto withdrawal(PaymentRequestDto dto);

    PaymentResponseDto trade();

    String createTrade(Long postId, Long among);
}
