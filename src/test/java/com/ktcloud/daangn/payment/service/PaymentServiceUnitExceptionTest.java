package com.ktcloud.daangn.payment.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import com.ktcloud.daangn.payment.dto.PaymentInitRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentTokenDto;
import com.ktcloud.daangn.payment.repository.PaymentRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceUnitExceptionTest {

    @Mock
    private MemberService memberService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PostService postService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Nested
    @DisplayName("입금 예외 테스트")
    class DepositExceptionTest {

        @Test
        @DisplayName("은행으로부터 중복된 거래 코드가 담긴 API 요청에 대한 예외를 발생한다.")
        public void deposit_DuplicateTranSeqNo_ThrowsException() {
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
        public void deposit_NonExistentMember_ThrowsException() {
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
        public void deposit_NullAmount_ThrowsException() {
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
        public void deposit_NegativeAmount_ThrowsException() {
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
        public void deposit_ZeroAmount_ThrowsException() {
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
    class WithdrawExceptionTest {

        @Test
        @DisplayName("은행으로부터 중복된 거래 코드가 담긴 API 요청에 대한 예외를 발생한다.")
        public void withdraw_DuplicateTranSeqNo_ThrowsException() {
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
        public void withdraw_NonExistentMember_ThrowsException() {
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
        public void withdraw_NegativeAmount_ThrowsException() {
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
        public void withdraw_ZeroAmount_ThrowsException() {
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
        @DisplayName("잔액 부족으로 인해 출금이 불가능할때 예외를 발생시킨다.")
        public void withdraw_InsufficientBalance_ThrowException() {
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

    @Nested
    @DisplayName("거래 생성 예외 테스트")
    class RequestPaymentExceptionTest {

        @Test
        @DisplayName("존재하지 않는 게시물은 거래를 생성 시 예외가 발생한다.")
        public void requestPayment_NonExistentPost_ThrowsException(){
            //given
            Long tranAmt = 5000L;
            Long postId = 1L;
            Long toMemberId = 1L;
            PaymentInitRequestDto dto = new PaymentInitRequestDto(toMemberId, postId, tranAmt);

            given(postService.getPostOrThrow(dto.postId())).willThrow(new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
            //when, then
            assertThatThrownBy(() -> paymentService.requestPayment(toMemberId, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 게시글이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("이미 판매된 상품은 거래를 생성 시 예외가 발생한다.")
        public void requestPayment_AlreadySoldPost_ThrowsException() {
            //given
            Address address = new Address("서울", "강남", "역삼");
            Long tranAmt = 5000L;
            Long postId = 1L;
            Long toMemberId = 1L;

            PaymentInitRequestDto dto = new PaymentInitRequestDto(toMemberId,postId, tranAmt);

            Member toMember = Member.builder()
                    .id(2L)
                    .email("test1@test.com")
                    .nickName("테스트2")
                    .balance(0L)
                    .address(address)
                    .build();

            Post targetPost = new Post(toMember, "제목", "내용", tranAmt, address);
            ReflectionTestUtils.setField(targetPost, "id", postId);
            ReflectionTestUtils.setField(targetPost, "status", PostStatus.SOLD);

            given(postService.getPostOrThrow(postId)).willReturn(targetPost);
            //when, then
            assertThatThrownBy(() -> paymentService.requestPayment(toMemberId,dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이미 판매된 제품입니다.");
        }
    }

    @Nested
    @DisplayName("거래 진행 예외 테스트")
    class confirmPaymentExceptionTest {

        @Test
        @DisplayName("이미 처리된 거래코드일 경우 예외가 발생한다.")
        public void confirmPayment_DuplicateTranSeqNo_ThrowsException() {
            //given
            Long tranAmt = 5000L;
            Long postId = 1L;
            String url = UuidCreator.getTimeOrderedEpoch() + "_" + tranAmt + "_" + postId;
            PaymentTokenDto dto = PaymentTokenDto.parse(url);

            given(paymentRepository.existsByTranSeqNo(dto.tranSeqNo())).willReturn(true);
            //when, then
            assertThatThrownBy(() -> paymentService.confirmPayment(1L, dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이미 진행된 거래입니다.");
        }

        @Test
        @DisplayName("게시물이 존재하지 않을 경우 예외가 발생한다.")
        public void confirmPayment_NonExistentPost_ThrowsException() {
            //given
            Long tranAmt = 5000L;
            Long postId = 1L;
            String url = UuidCreator.getTimeOrderedEpoch() + "_" + tranAmt + "_" + postId;
            PaymentTokenDto dto = PaymentTokenDto.parse(url);

            given(paymentRepository.existsByTranSeqNo(dto.tranSeqNo())).willReturn(false);
            given(postService.getPostOrThrowWithLock(postId)).willThrow(new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
            //when, then
            assertThatThrownBy(() -> paymentService.confirmPayment(1L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 게시글이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("본인의 게시물을 결제 할 수 없다.")
        public void confirmPayment_SelfTransaction_ThrowsException() {
            //given
            Long tranAmt = 5000L;
            Long postId = 1L;
            Address address = new Address("서울", "강남", "역삼");

            Member fromMember = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .nickName("테스트1")
                    .balance(5000L)
                    .address(address)
                    .build();

            Post targetPost = new Post(fromMember, "제목", "내용", tranAmt, address);
            ReflectionTestUtils.setField(targetPost, "id", postId);

            String url = UuidCreator.getTimeOrderedEpoch() + "_" + tranAmt + "_" + postId;
            PaymentTokenDto dto = PaymentTokenDto.parse(url);

            given(paymentRepository.existsByTranSeqNo(dto.tranSeqNo())).willReturn(false);
            given(postService.getPostOrThrowWithLock(postId)).willReturn(targetPost);
            //when, then
            assertThatThrownBy(() -> paymentService.confirmPayment(fromMember.getId(), dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("잘못된 접근입니다.");
        }

        @Test
        @DisplayName("이미 판매완료된 게시물일 경우 예외가 발생한다")
        public void confirmPayment_AlreadySoldPost_ThrowsException() {
            //given
            Long tranAmt = 5000L;
            Long postId = 1L;
            Long fromMemberId = 1L, toMemberId = 2L;
            Address address = new Address("서울", "강남", "역삼");

            Member toMember = Member.builder()
                    .id(toMemberId)
                    .email("test2@test.com")
                    .nickName("테스트2")
                    .balance(5000L)
                    .address(address)
                    .build();

            Post targetPost = new Post(toMember, "제목", "내용", tranAmt, address);
            ReflectionTestUtils.setField(targetPost, "id", postId);
            ReflectionTestUtils.setField(targetPost, "status", PostStatus.SOLD);

            String url = UuidCreator.getTimeOrderedEpoch() + "_" + tranAmt + "_" + postId;
            PaymentTokenDto dto = PaymentTokenDto.parse(url);

            given(paymentRepository.existsByTranSeqNo(dto.tranSeqNo())).willReturn(false);
            given(postService.getPostOrThrowWithLock(postId)).willReturn(targetPost);
            //when, then
            assertThatThrownBy(() -> paymentService.confirmPayment(fromMemberId, dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이미 판매된 제품입니다.");
        }

        @Test
        @DisplayName("구매자의 잔액이 부족할 경우 예외가 발생한다")
        public void confirmPayment_InsufficientBalance_ThrowsException() {
            //given
            Long tranAmt = 5000L;
            Long postId = 1L;
            Long fromMemberId = 1L, toMemberId = 2L;
            Address address = new Address("서울", "강남", "역삼");

            Member fromMember = Member.builder()
                    .id(fromMemberId)
                    .email("test1@test.com")
                    .nickName("테스트1")
                    .balance(0L)
                    .address(address)
                    .build();

            Member toMember = Member.builder()
                    .id(toMemberId)
                    .email("test2@test.com")
                    .nickName("테스트2")
                    .balance(5000L)
                    .address(address)
                    .build();


            Post targetPost = new Post(toMember, "제목", "내용", tranAmt, address);
            ReflectionTestUtils.setField(targetPost, "id", postId);

            String url = UuidCreator.getTimeOrderedEpoch() + "_" + tranAmt + "_" + postId;
            PaymentTokenDto dto = PaymentTokenDto.parse(url);

            given(paymentRepository.existsByTranSeqNo(dto.tranSeqNo())).willReturn(false);
            given(postService.getPostOrThrowWithLock(postId)).willReturn(targetPost);
            given(memberService.getByIdOrThrowWithLock(fromMemberId)).willReturn(fromMember);
            given(memberService.getByIdOrThrowWithLock(toMemberId)).willReturn(toMember);
            //when, then
            assertThatThrownBy(() -> paymentService.confirmPayment(fromMemberId, dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("잔액이 부족합니다.");
        }
    }
}
