package com.ktcloud.daangn.payment.service;

import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.payment.dto.PaymentInitRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.entity.PaymentHistory;
import com.ktcloud.daangn.post.entity.Post;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PaymentServiceIntegrationExceptionTest extends TestContainerConfig {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EntityManager em;

    private final Long TRAN_AMT = 5000L;
    private static final Long INITIAL_BALANCE = 5000L;

    private Long toMemberId;
    private Long fromMemberId;
    private Long postId;

    @BeforeEach
    public void initToMember() {
        Address address = new Address("서울시", "동작구", "사당동");
        Member member = Member.builder()
                .email("test1@test.com")
                .nickName("테스트")
                .balance(INITIAL_BALANCE)
                .address(address)
                .build();

        em.persist(member);
        em.flush();
        toMemberId = member.getId();
        em.clear();
    }

    @Nested
    @DisplayName("입금 통합 예외 테스트")
    class DepositExceptionTest {

        @Test
        @DisplayName("은행으로부터 중복된 거래 코드가 담긴 API 요청에 대한 예외를 발생한다.")
        public void deposit_DuplicateTranSeqNo_ThrowsException(){
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", TRAN_AMT, toMemberId);

            existingPaymentHistory(dto);
            //when, then
            assertThatThrownBy(() -> paymentService.deposit(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이미 진행된 내역입니다.");
        }

        @Test
        @DisplayName("은행으로부터 존재하지 않는 memberId가 담긴 API 요청에 대한 예외를 발생한다.")
        public void deposit_NonExistentMember_ThrowsException() {
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", TRAN_AMT, 99L);
            //when, then
            assertThatThrownBy(() -> paymentService.deposit(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("존재하지 않는 ID입니다.");
        }

        @Test
        @DisplayName("은행으로부터 입금 금액이 null인 요청에 대한 예외를 발생한다.")
        public void deposit_NullAmount_ThrowsException() {
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", null, toMemberId);
            //when, then
            assertThatThrownBy(() -> paymentService.deposit(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("입력값이 잘못되었습니다.");
        }

        @Test
        @DisplayName("은행으로부터 입금 금액이 음수인 요청에 대한 예외를 발생한다.")
        public void deposit_NegativeAmount_ThrowsException() {
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", -TRAN_AMT, toMemberId);
            //when, then
            assertThatThrownBy(() -> paymentService.deposit(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("입력값이 잘못되었습니다.");
        }

        @Test
        @DisplayName("은행으로부터 입금 금액이 0인 요청에 대한 예외를 발생한다.")
        public void deposit_ZeroAmount_ThrowsException() {
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", 0L, toMemberId);
            //when, then
            assertThatThrownBy(() -> paymentService.deposit(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("입력값이 잘못되었습니다.");
        }
    }

    @Nested
    @DisplayName("출금 통합 예외 테스트")
    class WithdrawExceptionTest {

        @Test
        @DisplayName("은행으로부터 중복된 거래 코드가 담긴 API 요청에 대한 예외를 발생한다.")
        public void withdraw_DuplicateTranSeqNo_ThrowsException() {
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", TRAN_AMT, toMemberId);

            existingPaymentHistory(dto);
            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이미 진행된 내역입니다.");
        }

        @Test
        @DisplayName("은행으로부터 존재하지 않는 memberId가 담긴 API 요청에 대한 예외를 발생한다.")
        public void withdraw_NonExistentMember_ThrowsException() {
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", TRAN_AMT, 99L);

            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("존재하지 않는 회원입니다.");
        }

        @Test
        @DisplayName("은행으로부터 출금 금액이 null인 요청에 대한 예외를 발생시킨다.")
        public void withdraw_NullAmount_ThrowsException() {
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", null, toMemberId);

            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("입력값이 잘못되었습니다.");
        }

        @Test
        @DisplayName("은행으로부터 출금 금액이 음수인 요청에 대한 예외를 발생시킨다.")
        public void withdraw_NegativeAmount_ThrowsException() {
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", -TRAN_AMT, toMemberId);

            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("입력값이 잘못되었습니다.");
        }

        @Test
        @DisplayName("은행으로부터 출금 금액이 0인 요청에 대한 예외를 발생시킨다.")
        public void withdraw_ZeroAmount_ThrowsException() {
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", 0L, toMemberId);

            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("입력값이 잘못되었습니다.");
        }

        @Test
        @DisplayName("잔액 부족으로 인해 출금이 불가능할때 예외를 발생시킨다.")
        public void withdraw_InsufficientBalance_ThrowException() {
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", INITIAL_BALANCE+1L, toMemberId);

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
            PaymentInitRequestDto dto = new PaymentInitRequestDto(99L, TRAN_AMT);
            //when, then
            assertThatThrownBy(() -> paymentService.requestPayment(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 게시글이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("이미 판매된 상품은 거래를 생성 시 예외가 발생한다.")
        public void requestPayment_AlreadySoldPost_ThrowsException() {
            //given
            initPost(true);

            PaymentInitRequestDto dto = new PaymentInitRequestDto(postId, TRAN_AMT);
            //when, then
            assertThatThrownBy(() -> paymentService.requestPayment(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 판매된 제품입니다.");
        }

    }

    private void existingPaymentHistory(PaymentRequestDto dto) {
        Member member = em.find(Member.class, dto.memberId());
        PaymentHistory paymentHistory = PaymentHistory.builder()
                .member(member)
                .tranSeqNo(dto.tran_seq_no())
                .changedCash(dto.tran_amt())
                .build();
        em.persist(paymentHistory);
        em.flush();
        em.clear();
    }

    private void initPost(Boolean isSold) {
        Member member = em.find(Member.class, toMemberId);
        Address address = new Address("서울시", "동작구", "사당동");
        Post post = Post.builder()
                .member(member)
                .title("제목")
                .content("내용")
                .price(TRAN_AMT)
                .location(address)
                .build();

        if (isSold) post.markAsSold();

        em.persist(post);
        em.flush();
        postId = post.getId();
        em.clear();
    }
}
