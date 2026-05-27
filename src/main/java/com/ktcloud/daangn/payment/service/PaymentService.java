package com.ktcloud.daangn.payment.service;

import com.ktcloud.daangn.payment.dto.PaymentInitRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;
import com.ktcloud.daangn.payment.dto.PaymentTokenDto;

public interface PaymentService {

    PaymentResponseDto deposit(PaymentRequestDto dto);

    PaymentResponseDto withdraw(PaymentRequestDto dto);

    PaymentResponseDto confirmPayment(Long fromMemberId, PaymentTokenDto dto);

    String requestPayment(PaymentInitRequestDto dto);
}
