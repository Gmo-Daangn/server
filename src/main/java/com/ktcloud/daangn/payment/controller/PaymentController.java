package com.ktcloud.daangn.payment.controller;

import com.ktcloud.daangn.auth.dto.CustomUser;
import com.ktcloud.daangn.common.dto.BaseResponse;
import com.ktcloud.daangn.payment.dto.*;
import com.ktcloud.daangn.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/deposit")
    public BaseResponse<PaymentResponseDto> deposit(@Valid @RequestBody PaymentRequestDto dto) {
        return BaseResponse.success(paymentService.deposit(dto));
    }

    @PostMapping("/withdraw")
    public BaseResponse<PaymentResponseDto> withdraw(@Valid @RequestBody PaymentRequestDto dto) {
        return BaseResponse.success(paymentService.withdraw(dto));
    }

    @PostMapping("/links")
    public BaseResponse<String> create(@Valid @RequestBody PaymentInitRequestDto dto) {
        return BaseResponse.success(paymentService.requestPayment(dto));
    }

    @GetMapping("/links/{tx}")
    public BaseResponse<PaymentTradeResponse> detail(@PathVariable String tx) {
        PaymentTokenDto dto = PaymentTokenDto.parse(tx);
        return BaseResponse.success(new PaymentTradeResponse(dto.postId(), dto.tranSeqNo(), dto.amount()));
    }

    @PostMapping("/links/{tx}")
    public BaseResponse<PaymentResponseDto> confirmPayment(@AuthenticationPrincipal CustomUser user, @PathVariable String tx) {
        Long fromMemberId = user.getMemberId();
        PaymentTokenDto dto = PaymentTokenDto.parse(tx);
        return BaseResponse.success(paymentService.confirmPayment(fromMemberId, dto));
    }

}
