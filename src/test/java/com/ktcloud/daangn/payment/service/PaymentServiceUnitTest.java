package com.ktcloud.daangn.payment.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.service.ChatMessageService;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import com.ktcloud.daangn.payment.dto.PaymentInitRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;
import com.ktcloud.daangn.payment.entity.PaymentHistory;
import com.ktcloud.daangn.payment.entity.PaymentToken;
import com.ktcloud.daangn.payment.entity.PaymentTokenStatus;
import com.ktcloud.daangn.payment.repository.PaymentRepository;
import com.ktcloud.daangn.payment.repository.PaymentTokenRepository;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.entity.PostStatus;
import com.ktcloud.daangn.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceUnitTest {

    @Mock private MemberService memberService;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PostService postService;
    @Mock private ChatMessageService chatMessageService;
    @Mock private PaymentTokenRepository paymentTokenRepository;

    @InjectMocks private PaymentServiceImpl paymentService;

    private static final Long INITIAL_BALANCE = 10000L;

    @Nested
    @DisplayName("입금 정상 테스트")
    class DepositTest{

        @Test
        @DisplayName("은행으로부터 정상적인 입금 확인 API 요청이 처리된다.")
        public void deposit_ValidRequest_Success(){
            //given
            Long tranAmt = 5000L;
            UUID tranSeqNo = UuidCreator.getTimeOrderedEpoch();
            PaymentRequestDto dto = new PaymentRequestDto(tranSeqNo, tranAmt, 1L);
            Member findMember = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .nickName("nickname")
                    .balance(INITIAL_BALANCE)
                    .address(new Address("서울", "강남", "역삼"))
                    .build();

            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(false);
            given(memberService.getByIdOrThrow(1L)).willReturn(findMember);
            //when
            PaymentResponseDto result = paymentService.deposit(dto);
            //then
            verify(paymentRepository).save(any(PaymentHistory.class));
            assertThat(result.name()).isEqualTo(findMember.getNickName());
            assertThat(result.balance()).isEqualTo(INITIAL_BALANCE + tranAmt);
        }
    }

    @Nested
    @DisplayName("출금 정상 테스트")
    class WithdrawTest{

        @Test
        @DisplayName("은행으로부터 정상적인 출금 확인 API 요청이 처리된다.")
        public void withdraw_ValidRequest_Success(){
            //given
            Long tranAmt = 5000L;
            UUID tranSeqNo = UuidCreator.getTimeOrderedEpoch();
            PaymentRequestDto dto = new PaymentRequestDto(tranSeqNo, tranAmt, 1L);
            Member findMember = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .nickName("nickname")
                    .balance(INITIAL_BALANCE)
                    .address(new Address("서울", "강남", "역삼"))
                    .build();

            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(false);
            given(memberService.getByIdOrThrow(1L)).willReturn(findMember);
            //when
            PaymentResponseDto result = paymentService.withdraw(dto);
            //then
            verify(paymentRepository).save(any(PaymentHistory.class));
            assertThat(result.name()).isEqualTo(findMember.getNickName());
            assertThat(result.balance()).isEqualTo(INITIAL_BALANCE - tranAmt);
        }
    }

    @Nested
    @DisplayName("거래 생성 정상 테스트")
    class RequestPaymentTest{

        @Test
        @DisplayName("거래 요청을 정상적으로 진행하여 링크 발급 진행한다.")
        public void requestPayment_ValidRequest_Success(){
            //given
            Address address = new Address("서울", "강남", "역삼");
            Long tranAmt = 5000L;
            Long postId = 1L;
            Long roomId = 1L;
            Long toMemberId = 1L;

            PaymentInitRequestDto dto = new PaymentInitRequestDto(roomId, postId, tranAmt);

            Member toMember = Member.builder()
                    .id(toMemberId)
                    .email("test1@test.com")
                    .nickName("테스트2")
                    .balance(INITIAL_BALANCE)
                    .address(address)
                    .build();

            Post targetPost = new Post(toMember, "제목", "내용", tranAmt, address);
            ReflectionTestUtils.setField(targetPost, "id", postId);

            given(postService.getPostOrThrow(postId)).willReturn(targetPost);
            given(chatMessageService.create(eq(roomId), eq(toMemberId), contains("결제 링크입니다!")))
                    .willReturn(mock(ChatMessageResponseDto.class));
            ReflectionTestUtils.setField(paymentService, "paymentLinkBaseUrl", "http://localhost:8080");
            //when
            paymentService.requestPayment(toMemberId, dto);
            // then
            verify(chatMessageService).create(eq(roomId), eq(toMemberId), contains("결제 링크입니다!"));
            verify(paymentTokenRepository).save(any(PaymentToken.class));
        }
    }

    @Nested
    @DisplayName("거래 진행 정상 테스트")
    class ConfirmPaymentTest{

        @Test
        @DisplayName("생성된 거래가 정상적으로 진행된다.")
        public void confirmPayment_ValidRequest_Success(){
            //given
            Address address = new Address("서울", "강남", "역삼");
            Long tranAmt = 5000L;
            Long fromMemberId = 1L;
            Long toMemberId = 2L;
            Long postId = 1L;

            Member fromMember = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .nickName("테스트1")
                    .balance(INITIAL_BALANCE)
                    .address(address)
                    .build();

            Member toMember = Member.builder()
                    .id(2L)
                    .email("test1@test.com")
                    .nickName("테스트2")
                    .balance(INITIAL_BALANCE)
                    .address(address)
                    .build();

            Post targetPost = new Post(toMember, "제목", "내용", tranAmt, address);
            ReflectionTestUtils.setField(targetPost, "id", postId);

            UUID tranSeqNo = UuidCreator.getTimeOrderedEpoch();
            String tx = tranSeqNo.toString();

            PaymentToken paymentToken = new PaymentToken(tranSeqNo,postId,toMemberId,tranAmt, PaymentTokenStatus.PENDING);
            given(memberService.getByIdOrThrowWithLock(fromMemberId)).willReturn(fromMember);
            given(memberService.getByIdOrThrowWithLock(toMemberId)).willReturn(toMember);
            given(postService.getPostOrThrowWithLock(postId)).willReturn(targetPost);
            given(paymentTokenRepository.getToken(tranSeqNo)).willReturn(Optional.of(paymentToken));
            //when
            PaymentResponseDto result = paymentService.confirmPayment(fromMember.getId(), tx);
            //then
            verify(paymentRepository, times(2)).save(any(PaymentHistory.class));
            assertThat(result.balance()).isEqualTo(fromMember.getBalance());
            assertThat(fromMember.getBalance()).isEqualTo(INITIAL_BALANCE - tranAmt);
            assertThat(result.balance()).isEqualTo(INITIAL_BALANCE - tranAmt);

            assertThat(toMember.getBalance()).isEqualTo(INITIAL_BALANCE + tranAmt);
            assertThat(targetPost.getStatus()).isEqualTo(PostStatus.SOLD);
            assertThat(paymentToken.getStatus()).isEqualTo(PaymentTokenStatus.COMPLETED);
        }
    }
}