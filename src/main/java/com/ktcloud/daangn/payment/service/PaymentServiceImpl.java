package com.ktcloud.daangn.payment.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import com.ktcloud.daangn.payment.dto.PaymentCreatTradeRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;
import com.ktcloud.daangn.payment.entity.PaymentHistory;
import com.ktcloud.daangn.payment.repository.PaymentRepository;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final MemberService memberService;
    private final PaymentRepository paymentRepository;
    private final PostService postService;

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
    public PaymentResponseDto trade(Long fromMemberId, String tranSeqNo, Long amount, Long postId) {
        if (paymentRepository.existsByTranSeqNo(tranSeqNo)) throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "이미 진행된 거래입니다.");
        Post post = postService.getPostOrThrow(postId);
        Member targetMember = memberService.getByIdOrThrow(post.getMember().getId());
        Member fromMember = memberService.getByIdOrThrow(fromMemberId);
        fromMember.changeBalance(false, amount);
        targetMember.changeBalance(true, amount);
        PaymentHistory fromMemberHistory = PaymentHistory.builder()
                .type("출금")
                .localDateTime(LocalDateTime.now())
                .member(fromMember)
                .balance(fromMember.getBalance())
                .changedCash(amount)
                .tranSeqNo(tranSeqNo)
                .build();
        PaymentHistory targetMemberHistory = PaymentHistory.builder()
                .type("출금")
                .localDateTime(LocalDateTime.now())
                .member(targetMember)
                .balance(targetMember.getBalance())
                .changedCash(amount)
                .tranSeqNo(tranSeqNo)
                .build();

        paymentRepository.save(fromMemberHistory);
        paymentRepository.save(targetMemberHistory);

        return new PaymentResponseDto(fromMember.getNickName(), fromMember.getBalance());
    }

    @Override
    public String createTrade(PaymentCreatTradeRequestDto dto) {
        UUID tranSeqNo = UuidCreator.getTimeOrderedEpoch();
        //todo 추후 among과 postId는 외부로 노출 하지않는 방향으로 변경 예정
        return tranSeqNo+"_"+dto.amount()+"_"+dto.postId();
    }
}
