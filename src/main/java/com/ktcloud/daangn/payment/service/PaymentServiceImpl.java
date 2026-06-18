package com.ktcloud.daangn.payment.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ktcloud.daangn.chat.service.ChatMessageService;
import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import com.ktcloud.daangn.payment.dto.PaymentInitRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;
import com.ktcloud.daangn.payment.dto.PaymentTokenDto;
import com.ktcloud.daangn.payment.entity.PaymentHistory;
import com.ktcloud.daangn.payment.entity.PaymentStatus;
import com.ktcloud.daangn.payment.entity.PaymentToken;
import com.ktcloud.daangn.payment.entity.PaymentTokenStatus;
import com.ktcloud.daangn.payment.repository.PaymentRepository;
import com.ktcloud.daangn.payment.repository.PaymentTokenRepository;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.entity.PostStatus;
import com.ktcloud.daangn.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final ChatMessageService chatMessageService;
    private final PaymentTokenRepository paymentTokenRepository;

    @Value("${app.payment-link-base-url}")
    private String paymentLinkBaseUrl;

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
    public PaymentResponseDto withdraw(PaymentRequestDto dto) {
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
    public PaymentResponseDto confirmPayment(Long fromMemberId, UUID tx) {
        PaymentToken paymentToken = paymentTokenRepository.getTokenWithLock(tx)
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "잘못된 접근입니다."));
        if (paymentToken.isCompleted()) throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "이미 진행된 거래입니다.");
        PaymentTokenDto dto = PaymentTokenDto.from(paymentToken);

        Post post = postService.getPostOrThrowWithLock(dto.postId());
        Long targetMemberId = post.getMemberId();

        if (fromMemberId.equals(targetMemberId)) throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "잘못된 접근입니다.");
        if (post.getStatus().equals(PostStatus.SOLD)) throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "이미 판매된 제품입니다.");

        Member fromMember, targetMember;
        if (fromMemberId < targetMemberId) {
            fromMember = memberService.getByIdOrThrowWithLock(fromMemberId);
            targetMember = memberService.getByIdOrThrowWithLock(targetMemberId);
        } else {
            targetMember = memberService.getByIdOrThrowWithLock(targetMemberId);
            fromMember = memberService.getByIdOrThrowWithLock(fromMemberId);
        }

        fromMember.changeBalance(false, dto.amount());
        targetMember.changeBalance(true, dto.amount());

        PaymentHistory fromMemberHistory = PaymentHistory.builder()
                .type(PaymentStatus.WITHDRAWAL)
                .localDateTime(LocalDateTime.now())
                .member(fromMember)
                .balance(fromMember.getBalance())
                .changedCash(dto.amount())
                .tranSeqNo(dto.tranSeqNo())
                .build();

        PaymentHistory targetMemberHistory = PaymentHistory.builder()
                .type(PaymentStatus.DEPOSIT)
                .localDateTime(LocalDateTime.now())
                .member(targetMember)
                .balance(targetMember.getBalance())
                .changedCash(dto.amount())
                .tranSeqNo(dto.tranSeqNo())
                .build();

        post.markAsSold();
        paymentToken.markAsCompleted();

        paymentRepository.save(fromMemberHistory);
        paymentRepository.save(targetMemberHistory);

        return new PaymentResponseDto(fromMember.getNickName(), fromMember.getBalance());
    }

    @Override
    public void requestPayment(Long sellerId,PaymentInitRequestDto dto) {
        Post post = postService.getPostOrThrow(dto.postId());

        if (post.getStatus().equals(PostStatus.SOLD)) throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "이미 판매된 제품입니다.");

        UUID tranSeqNo = UuidCreator.getTimeOrderedEpoch();
        PaymentToken paymentToken = PaymentToken.builder()
                .tranSeqNo(tranSeqNo)
                .postId(dto.postId())
                .sellerId(sellerId)
                .amount(dto.amount())
                .status(PaymentTokenStatus.PENDING)
                .build();

        paymentTokenRepository.save(paymentToken);

        String message = "결제 링크입니다!\n" + paymentLinkBaseUrl + "/api/v1/payments/links/" + tranSeqNo;
        chatMessageService.create(dto.roomId(), sellerId, message);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentTokenDto getTokenInfo(UUID token) {
        PaymentToken findToken = paymentTokenRepository.getToken(token)
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "잘못된 링크입니다."));
        if (findToken.isCompleted()) throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "이미 진행된 거래입니다.");
        return PaymentTokenDto.from(findToken);
    }
}
