package com.ktcloud.daangn.payment.service;

import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.repository.PaymentRepository;
import com.ktcloud.daangn.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceUnitExceptionTest {

    @Mock
    private MemberService memberService;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PostService postService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Nested
    @DisplayName("입금 예외 테스트")
    class DepositExceptionTest{

        @Test
        @DisplayName("은행으로부터 중복된 거래 코드가 담긴 API 요청에 대한 예외를 발생한다.")
        public void deposit_DuplicateTranSeqNo_ThrowsException(){
            //given
            Long tran_amt = 5000L;
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tran_amt, 1L);

            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(true);
            //when, then
            assertThatThrownBy(() -> paymentService.deposit(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이미 진행된 내역입니다.");
        }

        @Test
        @DisplayName("은행으로부터 존재하지 않는 memberId가 담긴 API 요청에 대한 예외를 발생한다.")
        public void deposit_NonExistentMember_ThrowsException(){
            //given
            Long tran_amt = 5000L;
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tran_amt, 99L);

            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(false);
            given(memberService.getByIdOrThrow(dto.memberId())).willThrow(new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 회원입니다."));
            //when, then
            assertThatThrownBy(() -> paymentService.deposit(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("존재하지 않는 회원입니다.");

        }

        @Test
        @DisplayName("은행으로부터 입금 금액이 null인 요청에 대한 예외를 발생한다.")
        public void deposit_NullAmount_ThrowsException(){
            //given
            Long tran_amt = null;

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
            //when, then
            assertThatThrownBy(() -> paymentService.deposit(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("입력값이 잘못되었습니다.");
        }

        @Test
        @DisplayName("은행으로부터 입금 금액이 음수인 요청에 대한 예외를 발생한다.")
        public void deposit_NegativeAmount_ThrowsException(){
            //given
            Long tran_amt = -5000L;

            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tran_amt, 1L);
            Member findMember = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .nickName("nickname")
                    .balance(0L)
                    .address(new Address("서울", "강남", "역삼"))
                    .build();
            //when
            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(false);
            given(memberService.getByIdOrThrow(1L)).willReturn(findMember);
            //then
            assertThatThrownBy(() -> paymentService.deposit(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("입력값이 잘못되었습니다.");
        }

        @Test
        @DisplayName("은행으로부터 입금 금액이 0인 요청에 대한 예외를 발생한다.")
        public void deposit_ZeroAmount_ThrowsException(){
            //given
            Long tran_amt = 0L;

            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tran_amt, 1L);
            Member findMember = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .nickName("nickname")
                    .balance(0L)
                    .address(new Address("서울", "강남", "역삼"))
                    .build();
            //when
            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(false);
            given(memberService.getByIdOrThrow(1L)).willReturn(findMember);
            //then
            assertThatThrownBy(() -> paymentService.deposit(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("입력값이 잘못되었습니다.");
        }
    }

    @Nested
    @DisplayName("출금 예외 테스트")
    class WithdrawExceptionTest{

        @Test
        @DisplayName("은행으로부터 중복된 거래 코드가 담긴 API 요청에 대한 예외를 발생한다.")
        public void withdraw_DuplicateTranSeqNo_ThrowsException(){
            //given
            Long tran_amt = 5000L;
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tran_amt, 1L);

            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(true);
            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이미 진행된 내역입니다.");
        }

        @Test
        @DisplayName("은행으로부터 존재하지 않는 memberId가 담긴 API 요청에 대한 예외를 발생한다.")
        public void withdraw_NonExistentMember_ThrowsException(){
            //given
            Long tran_amt = 5000L;
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tran_amt, 99L);

            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(false);
            given(memberService.getByIdOrThrow(dto.memberId())).willThrow(new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 회원입니다."));
            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("존재하지 않는 회원입니다.");
        }

        @Test
        @DisplayName("은행으로부터 출금 금액이 null인 요청에 대한 예외를 발생시킨다.")
        public void withdraw_NullAmount_ThrowsException() {
            //given

            Long tran_amt = null;

            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tran_amt, 99L);
            Member findMember = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .nickName("nickname")
                    .balance(5000L)
                    .address(new Address("서울", "강남", "역삼"))
                    .build();

            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(false);
            given(memberService.getByIdOrThrow(dto.memberId())).willReturn(findMember);
            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("입력값이 잘못되었습니다.");
        }

        @Test
        @DisplayName("은행으로부터 출금 금액이 음수인 요청에 대한 예외를 발생시킨다.")
        public void withdraw_NegativeAmount_ThrowsException(){
            //given
            Long tran_amt = -5000L;

            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tran_amt, 99L);
            Member findMember = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .nickName("nickname")
                    .balance(5000L)
                    .address(new Address("서울", "강남", "역삼"))
                    .build();

            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(false);
            given(memberService.getByIdOrThrow(dto.memberId())).willReturn(findMember);
            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("입력값이 잘못되었습니다.");
        }
        
        @Test
        @DisplayName("은행으로부터 출금 금액이 0인 요청에 대한 예외를 발생시킨다.")
        public void withdraw_ZeroAmount_ThrowsException(){
            //given
            Long tran_amt = 0L;

            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tran_amt, 99L);
            Member findMember = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .nickName("nickname")
                    .balance(5000L)
                    .address(new Address("서울", "강남", "역삼"))
                    .build();

            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(false);
            given(memberService.getByIdOrThrow(dto.memberId())).willReturn(findMember);
            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("입력값이 잘못되었습니다.");
        }

        @Test
        @DisplayName("")
        public void withdraw_InsufficientBalance_ThrowException(){
            //given
            Long tran_amt = 5000L;

            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tran_amt, 99L);
            Member findMember = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .nickName("nickname")
                    .balance(0L)
                    .address(new Address("서울", "강남", "역삼"))
                    .build();

            given(paymentRepository.existsByTranSeqNo(dto.tran_seq_no())).willReturn(false);
            given(memberService.getByIdOrThrow(dto.memberId())).willReturn(findMember);
            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("잔액이 부족합니다.");
        }
    }
}
