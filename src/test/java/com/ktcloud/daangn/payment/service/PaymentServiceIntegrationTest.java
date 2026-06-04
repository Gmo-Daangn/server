package com.ktcloud.daangn.payment.service;

import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentServiceImpl paymentService;
    @Autowired
    private EntityManager em;

    private static final Long INITIAL_BALANCE = 10000L;// 초기 잔액
    private static final String INITIAL_NAME = "테스트";

    @BeforeEach
    public void createMember(){

        Address address = new Address("서울시", "동작구", "사당동");
        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickName(INITIAL_NAME)
                .balance(INITIAL_BALANCE)
                .address(address)
                .build();

        em.persist(member);
        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("입금 통합 테스트")
    class DepositTest{

        @Test
        @DisplayName("은행으로부터 정상적인 입금 확인 API 요청이 처리된다.")
        public void deposit_ValidRequest_Success(){
            //given
            Long tranAmt = 5000L;

            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tranAmt, 1L);
            //when
            PaymentResponseDto result = paymentService.deposit(dto);
            //then
            assertThat(result.name()).isEqualTo(INITIAL_NAME);
            assertThat(result.balance()).isEqualTo(INITIAL_BALANCE + tranAmt);

            Member findMember = em.find(Member.class, dto.memberId());
            assertThat(findMember.getBalance()).isEqualTo(INITIAL_BALANCE + tranAmt);
        }
    }

    @Nested
    @DisplayName("출금 통합 테스트")
    class WithdrawTest{

        @Test
        @DisplayName("은행으로부터 정상적인 출금 확인 API 요청이 처리된다.")
        public void withdraw_ValidRequest_Success(){
            //given
            Long tranAmt = 5000L;

            PaymentRequestDto dto = new PaymentRequestDto("tx123123123asd", tranAmt, 1L);
            //when
            PaymentResponseDto result = paymentService.withdraw(dto);
            //then
            assertThat(result.name()).isEqualTo(INITIAL_NAME);
            assertThat(result.balance()).isEqualTo(INITIAL_BALANCE - tranAmt);

            Member findMember = em.find(Member.class, dto.memberId());
            assertThat(findMember.getBalance()).isEqualTo(INITIAL_BALANCE - tranAmt);
        }
    }
}
