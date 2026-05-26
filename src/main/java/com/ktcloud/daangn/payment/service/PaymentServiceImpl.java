package com.ktcloud.daangn.payment.service;

import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;
import com.ktcloud.daangn.payment.entity.PaymentHistory;
import com.ktcloud.daangn.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final MemberService memberService;
    private final PaymentRepository paymentRepository;

    @Override
    public PaymentResponseDto deposit(PaymentRequestDto dto) {
        if (paymentRepository.existsByTranSeqNo(dto.tran_seq_no())) {
            throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "이미 진행된 내역입니다.");
        }
        Member member = memberService.getByIdOrThrow(dto.memberId());
        member.changeBalance(true, dto.tran_amt());
        PaymentHistory paymentHistory = dto.to(member, true);
        paymentRepository.save(paymentHistory);
        return new PaymentResponseDto(member.getNickName(), member.getBalance());
    }

    @Override
    public PaymentResponseDto withdrawal(PaymentRequestDto dto) {
        if (paymentRepository.existsByTranSeqNo(dto.tran_seq_no())) {
            throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "이미 진행된 내역입니다.");
        }
        Member member = memberService.getByIdOrThrow(dto.memberId());
        member.changeBalance(false, dto.tran_amt());
        PaymentHistory paymentHistory = dto.to(member, false);
        paymentRepository.save(paymentHistory);
        return new PaymentResponseDto(member.getNickName(), member.getBalance());
    }

    @Override
    public PaymentResponseDto trade() {
        return null;
    }

    @Override
    public String createTrade() {
        return "";
    }
}
