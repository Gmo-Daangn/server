package com.ktcloud.daangn.payment.service;

import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;
import com.ktcloud.daangn.payment.entity.PaymentHistory;
import com.ktcloud.daangn.payment.repository.PaymentRepository;
import com.ktcloud.daangn.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceUnitTest {

    @Mock private MemberService memberService;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PostService postService;

    @InjectMocks private PaymentServiceImpl paymentService;

    @Nested
    @DisplayName("입금 정상 테스트")
    class DepositTest{

        @Test
        @DisplayName("은행으로부터 정상적인 입금 확인 API 요청이 처리된다.")
        public void deposit_ValidRequest_Success(){
            //given
            Long tran_amt = 5000L;
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tran_amt, 1L);
            Member findMember = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .nickName("nickname")
                    .balance(0L)
                    .address(new Address("서울", "강남", "역삼"))
                    .build();

            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(false);
            given(memberService.getByIdOrThrow(1L)).willReturn(findMember);
            //when
            PaymentResponseDto result = paymentService.deposit(dto);
            //then
            verify(paymentRepository).save(any(PaymentHistory.class));
            assertThat(result.name()).isEqualTo(findMember.getNickName());
            assertThat(result.balance()).isEqualTo(findMember.getBalance());
        }
    }

    @Nested
    @DisplayName("출금 정상 테스트")
    class WithdrawTest{

        @Test
        @DisplayName("은행으로부터 정상적인 출금 확인 API 요청이 처리된다.")
        public void withdraw_ValidRequest_Success(){
            //given
            Long tran_amt = 5000L;
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tran_amt, 1L);
            Member findMember = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .nickName("nickname")
                    .balance(tran_amt)
                    .address(new Address("서울", "강남", "역삼"))
                    .build();

            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(false);
            given(memberService.getByIdOrThrow(1L)).willReturn(findMember);
            //when
            PaymentResponseDto result = paymentService.withdraw(dto);
            //then
            verify(paymentRepository).save(any(PaymentHistory.class));
            assertThat(result.name()).isEqualTo(findMember.getNickName());
            assertThat(result.balance()).isEqualTo(0L);
        }
    }
}