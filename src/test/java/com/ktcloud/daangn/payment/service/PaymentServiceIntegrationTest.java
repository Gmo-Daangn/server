package com.ktcloud.daangn.payment.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ktcloud.daangn.chat.entity.ChatMessage;
import com.ktcloud.daangn.chat.entity.ChatParticipant;
import com.ktcloud.daangn.chat.entity.ChatRoom;
import com.ktcloud.daangn.chat.entity.ChatType;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.payment.dto.PaymentInitRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;
import com.ktcloud.daangn.payment.entity.PaymentHistory;
import com.ktcloud.daangn.payment.entity.PaymentToken;
import com.ktcloud.daangn.payment.entity.PaymentTokenStatus;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.entity.PostStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PaymentServiceIntegrationTest extends TestContainerConfig {

    @Autowired
    private PaymentServiceImpl paymentService;
    @Autowired
    private EntityManager em;

    private static final Long INITIAL_BALANCE = 10000L;// 초기 잔액
    private static final String INITIAL_NAME = "테스트";
    private static final Long TRAN_AMT = 5000L;
    private Long toMemberId;
    private Long fromMemberId;
    private Long postId;
    private Long roomId;

    @BeforeEach
    public void initToMember(){
        Address address = new Address("서울시", "동작구", "사당동");
        Member member = Member.builder()
                .email("test1@test.com")
                .nickName(INITIAL_NAME)
                .balance(INITIAL_BALANCE)
                .address(address)
                .build();

        em.persist(member);
        em.flush();
        toMemberId = member.getId();
        em.clear();
    }

    @Nested
    @DisplayName("입금 통합 테스트")
    class DepositTest{

        @Test
        @DisplayName("은행으로부터 정상적인 입금 확인 API 요청이 처리된다.")
        public void deposit_ValidRequest_Success(){
            //given
            UUID tranSeqNo = UuidCreator.getTimeOrderedEpoch();
            PaymentRequestDto dto = new PaymentRequestDto(tranSeqNo, TRAN_AMT, toMemberId);
            //when
            PaymentResponseDto result = paymentService.deposit(dto);
            //then
            assertThat(result.name()).isEqualTo(INITIAL_NAME);
            assertThat(result.balance()).isEqualTo(INITIAL_BALANCE + TRAN_AMT);

            Member findMember = em.find(Member.class, dto.memberId());
            assertThat(findMember.getBalance()).isEqualTo(INITIAL_BALANCE + TRAN_AMT);
        }
    }

    @Nested
    @DisplayName("출금 통합 테스트")
    class WithdrawTest{

        @Test
        @DisplayName("은행으로부터 정상적인 출금 확인 API 요청이 처리된다.")
        public void withdraw_ValidRequest_Success(){
            //given
            UUID tranSeqNo = UuidCreator.getTimeOrderedEpoch();
            PaymentRequestDto dto = new PaymentRequestDto(tranSeqNo, TRAN_AMT, toMemberId);
            //when
            PaymentResponseDto result = paymentService.withdraw(dto);
            //then
            assertThat(result.name()).isEqualTo(INITIAL_NAME);
            assertThat(result.balance()).isEqualTo(INITIAL_BALANCE - TRAN_AMT);

            Member findMember = em.find(Member.class, dto.memberId());
            assertThat(findMember.getBalance()).isEqualTo(INITIAL_BALANCE - TRAN_AMT);
        }
    }

    @Nested
    @DisplayName("거래 생성 통합 테스트")
    class RequestPaymentTest {

        @Test
        @DisplayName("거래 요청을 정상적으로 진행하여 링크 발급 진행한다.")
        public void requestPayment_ValidRequest_Success(){
            //given
            initPost();
            initFromMember();
            initChatRoom();

            PaymentInitRequestDto dto = new PaymentInitRequestDto(roomId, postId, TRAN_AMT);
            //when
            paymentService.requestPayment(toMemberId, dto);
            //then
            ChatMessage chatMessage = em.createQuery("select c from ChatMessage c", ChatMessage.class)
                    .getSingleResult();
            assertThat(chatMessage.getMessage()).contains("결제 링크입니다!");
            assertThat(chatMessage.getMember().getId()).isEqualTo(toMemberId);

            List<PaymentToken> resultList = em.createQuery("select p from PaymentToken p order by p.tranSeqNo asc", PaymentToken.class)
                    .getResultList();
            assertThat(resultList).hasSize(1);
        }
    }

    @Nested
    @DisplayName("거래 진행 통합 테스트")
    class ConfirmPaymentTest {

        @Test
        @DisplayName("생성된 거래가 정상적으로 진행된다.")
        public void confirmPayment_ValidRequest_Success(){
            //given
            initPost();
            initFromMember();

            // PaymentToken DB에 직접 저장
            UUID tranSeqNo = UuidCreator.getTimeOrderedEpoch();
            PaymentToken paymentToken = PaymentToken.builder()
                    .tranSeqNo(tranSeqNo)
                    .postId(postId)
                    .sellerId(toMemberId)
                    .amount(TRAN_AMT)
                    .status(PaymentTokenStatus.PENDING)
                    .build();
            em.persist(paymentToken);
            em.flush();
            em.clear();

            String tx = tranSeqNo.toString();
            //when
            paymentService.confirmPayment(fromMemberId, tx);
            //then
            Member toMember = em.find(Member.class, toMemberId);
            Member fromMember = em.find(Member.class, fromMemberId);
            assertThat(toMember.getBalance()).isEqualTo(INITIAL_BALANCE + TRAN_AMT);
            assertThat(fromMember.getBalance()).isEqualTo(INITIAL_BALANCE - TRAN_AMT);

            Post post = em.find(Post.class, postId);
            assertThat(post.getStatus()).isEqualTo(PostStatus.SOLD);

            List<PaymentHistory> historyList = em.createQuery(
                            "SELECT p FROM PaymentHistory p ORDER BY p.id ASC", PaymentHistory.class)
                    .getResultList();
            assertThat(historyList).hasSize(2);
            assertThat(historyList).extracting(PaymentHistory::getTranSeqNo)
                    .containsOnly(tranSeqNo);

            PaymentToken updatedToken = em.find(PaymentToken.class, tranSeqNo);
            assertThat(updatedToken.getStatus()).isEqualTo(PaymentTokenStatus.COMPLETED);
        }
    }

    private void initFromMember (){
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

    private void initPost() {
        Address address = new Address("서울시", "동작구", "사당동");
        Member toMember = em.find(Member.class, toMemberId);
        Post targetPost = new Post(toMember, "제목", "내용", TRAN_AMT, address);
        em.persist(targetPost);
        em.flush();
        postId = targetPost.getId();
        em.clear();
    }

    private void initChatRoom() {
        Member seller = em.find(Member.class, toMemberId);
        Member buyer = em.find(Member.class, fromMemberId);

        ChatRoom chatRoom = ChatRoom.createRoom(postId, ChatType.PRODUCT);
        em.persist(chatRoom);

        em.persist(ChatParticipant.createParticipant(chatRoom, seller));
        em.persist(ChatParticipant.createParticipant(chatRoom, buyer));

        em.flush();
        roomId = chatRoom.getId();
        em.clear();
    }
}
