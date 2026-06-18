package com.ktcloud.daangn.payment.service;

import com.ktcloud.daangn.payment.dto.PaymentInitRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;
import com.ktcloud.daangn.payment.dto.PaymentTokenDto;

import java.util.UUID;

public interface PaymentService {

    PaymentResponseDto deposit(PaymentRequestDto dto);

    PaymentResponseDto withdraw(PaymentRequestDto dto);

    PaymentResponseDto confirmPayment(Long fromMemberId, UUID tx);

    void requestPayment(Long sellerId,PaymentInitRequestDto dto);

    PaymentTokenDto getTokenInfo(UUID token);
}
