package com.ktcloud.daangn.payment.controller;

import com.ktcloud.daangn.auth.dto.CustomUser;
import com.ktcloud.daangn.common.dto.BaseResponse;
import com.ktcloud.daangn.payment.dto.PaymentCreatTradeRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;
import com.ktcloud.daangn.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/deposit")
    public BaseResponse<PaymentResponseDto> deposit(@Valid @RequestBody PaymentRequestDto dto){
        return BaseResponse.success(paymentService.deposit(dto));
    }

    @PostMapping("/withdrawal")
    public BaseResponse<PaymentResponseDto> withdrawal(@Valid @RequestBody PaymentRequestDto dto) {
        return BaseResponse.success(paymentService.withdrawal(dto));
    }

    @PostMapping("/links")
    public BaseResponse<String> createLink(@Valid @RequestBody PaymentCreatTradeRequestDto dto){
        return BaseResponse.success(paymentService.createTrade(dto));
    }

    @PostMapping("/links/{tx}")
    public BaseResponse<PaymentResponseDto> trade(@AuthenticationPrincipal CustomUser user, @PathVariable String tx) {
        Long fromMemberId = user.getMemberId();
        String[] parts = tx.split("_");
        String tranSeqNo = parts[0];
        Long amount = Long.parseLong(parts[1]);
        Long postId = Long.parseLong(parts[2]);
        return BaseResponse.success(paymentService.trade(fromMemberId, tranSeqNo, amount, postId));
    }
}
