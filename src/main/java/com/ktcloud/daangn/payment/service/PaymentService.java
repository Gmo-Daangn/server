package com.ktcloud.daangn.payment.service;

import com.ktcloud.daangn.payment.dto.PaymentInitRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;

public interface PaymentService {

    PaymentResponseDto deposit(PaymentRequestDto dto);

    PaymentResponseDto withdraw(PaymentRequestDto dto);

    PaymentResponseDto confirmPayment(Long fromMemberId, String tranSeqNo, Long among, Long postId);

    String requestPayment(PaymentInitRequestDto dto);
}
