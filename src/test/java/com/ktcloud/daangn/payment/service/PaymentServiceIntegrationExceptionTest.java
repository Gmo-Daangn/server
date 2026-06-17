package com.ktcloud.daangn.payment.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.payment.dto.PaymentInitRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentTokenDto;
import com.ktcloud.daangn.payment.entity.PaymentHistory;
import com.ktcloud.daangn.payment.entity.PaymentStatus;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

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
    private static final Long NON_EXISTENT_ID = Long.MAX_VALUE;

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

            Member member = em.find(Member.class, dto.memberId());
            PaymentHistory paymentHistory = PaymentHistory.builder()
                    .member(member)
                    .tranSeqNo(dto.tran_seq_no())
                    .changedCash(dto.tran_amt())
                    .type(PaymentStatus.DEPOSIT)
                    .balance(member.getBalance())
                    .localDateTime(LocalDateTime.now())
                    .build();
            em.persist(paymentHistory);
            em.flush();
            em.clear();

            //when, then
            assertThatThrownBy(() -> paymentService.deposit(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이미 진행된 내역입니다.");
        }

        @Test
        @DisplayName("은행으로부터 존재하지 않는 memberId가 담긴 API 요청에 대한 예외를 발생한다.")
        public void deposit_NonExistentMember_ThrowsException() {
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", TRAN_AMT, NON_EXISTENT_ID);
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

            Member member = em.find(Member.class, dto.memberId());
            PaymentHistory paymentHistory = PaymentHistory.builder()
                    .member(member)
                    .tranSeqNo(dto.tran_seq_no())
                    .changedCash(dto.tran_amt())
                    .type(PaymentStatus.WITHDRAWAL)
                    .balance(member.getBalance())
                    .localDateTime(LocalDateTime.now())
                    .build();
            em.persist(paymentHistory);
            em.flush();
            em.clear();

            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이미 진행된 내역입니다.");
        }

        @Test
        @DisplayName("은행으로부터 존재하지 않는 memberId가 담긴 API 요청에 대한 예외를 발생한다.")
        public void withdraw_NonExistentMember_ThrowsException() {
            //given
            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", TRAN_AMT, NON_EXISTENT_ID);

            //when, then
            assertThatThrownBy(() -> paymentService.withdraw(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("존재하지 않는 ID입니다.");
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
            PaymentInitRequestDto dto = new PaymentInitRequestDto(toMemberId, NON_EXISTENT_ID, TRAN_AMT);
            //when, then
            assertThatThrownBy(() -> paymentService.requestPayment(toMemberId, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 게시글이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("이미 판매된 상품은 거래를 생성 시 예외가 발생한다.")
        public void requestPayment_AlreadySoldPost_ThrowsException() {
            //given
            initPost(true);

            PaymentInitRequestDto dto = new PaymentInitRequestDto(toMemberId, postId, TRAN_AMT);
            //when, then
            assertThatThrownBy(() -> paymentService.requestPayment(toMemberId, dto))
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
            PaymentTokenDto dto = PaymentTokenDto.parse(initUrl(false, TRAN_AMT));

            Member member = em.find(Member.class, toMemberId);
            PaymentHistory paymentHistory = PaymentHistory.builder()
                    .member(member)
                    .tranSeqNo(dto.tranSeqNo())
                    .changedCash(dto.amount())
                    .type(PaymentStatus.DEPOSIT)
                    .balance(member.getBalance())
                    .localDateTime(LocalDateTime.now())
                    .build();
            em.persist(paymentHistory);
            em.flush();
            em.clear();

            initFromMember();
            //when, then
            assertThatThrownBy(() -> paymentService.confirmPayment(fromMemberId, dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이미 진행된 거래입니다.");
        }

        @Test
        @DisplayName("게시물이 존재하지 않을 경우 예외가 발생한다.")
        public void confirmPayment_NonExistentPost_ThrowsException() {
            //given
            String[] paseUrl = initUrl(false, TRAN_AMT).split("_");
            String tranSeqNo = paseUrl[0];
            Long amount = Long.parseLong(paseUrl[1]);
            PaymentTokenDto dto = new PaymentTokenDto(tranSeqNo, amount, NON_EXISTENT_ID);
            initFromMember();

            //when, then
            assertThatThrownBy(() -> paymentService.confirmPayment(fromMemberId, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 게시글이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("본인의 게시물을 결제 할 수 없다.")
        public void confirmPayment_SelfTransaction_ThrowsException() {
            //given
            PaymentTokenDto dto = PaymentTokenDto.parse(initUrl(false, TRAN_AMT));

            //when, then
            assertThatThrownBy(() -> paymentService.confirmPayment(toMemberId, dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("잘못된 접근입니다.");
        }

        @Test
        @DisplayName("이미 판매완료된 게시물일 경우 예외가 발생한다")
        public void confirmPayment_AlreadySoldPost_ThrowsException() {
            //given
            PaymentTokenDto dto = PaymentTokenDto.parse(initUrl(true, TRAN_AMT));
            initFromMember();

            //when, then
            assertThatThrownBy(() -> paymentService.confirmPayment(fromMemberId, dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이미 판매된 제품입니다.");
        }

        @Test
        @DisplayName("구매자의 잔액이 부족할 경우 예외가 발생한다")
        public void confirmPayment_InsufficientBalance_ThrowsException() {
            //given
            PaymentTokenDto dto = PaymentTokenDto.parse(initUrl(false, INITIAL_BALANCE + 1L));
            initFromMember();

            //when, then 예외 학인
            assertThatThrownBy(() -> paymentService.confirmPayment(fromMemberId, dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("잔액이 부족합니다.");

            em.flush();
            em.clear();
            //then DB에 잔액 변경이 반영되지 않고 원래대로 남아있는지 검증
            Member fromMember = em.find(Member.class, fromMemberId);
            Member toMember = em.find(Member.class, toMemberId);

            assertThat(fromMember.getBalance()).isEqualTo(INITIAL_BALANCE);
            assertThat(toMember.getBalance()).isEqualTo(INITIAL_BALANCE);
        }
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

    public void initFromMember() {
        Address address = new Address("서울시", "동작구", "사당동");
        Member member = Member.builder()
                .email("test2@test.com")
                .nickName("구매자")
                .balance(INITIAL_BALANCE)
                .address(address)
                .build();

        em.persist(member);
        em.flush();
        fromMemberId = member.getId();
        em.clear();
    }

    private String initUrl(Boolean isSold, Long tranAmt) {
        initPost(isSold);
        return UuidCreator.getTimeOrderedEpoch() + "_" + tranAmt + "_" + postId;
    }
}
