package com.ktcloud.daangn.payment.service;

import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.payment.dto.PaymentTokenDto;
import com.ktcloud.daangn.payment.entity.PaymentHistory;
import com.ktcloud.daangn.payment.entity.PaymentStatus;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.entity.PostStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentServiceConcurrencyTest extends TestContainerConfig {

    @Autowired PaymentService paymentService;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;

    private final Long POST_PRICE = 5000L;
    private static final Long INITIAL_BALANCE = 5000L;
    private static final String INITIAL_NAME = "테스트";
    private static final Address ADDRESS = new Address("서울시", "동작구", "사당동");

    private Long mainMemberId;
    private final List<Long> counterpartMemberIds = new ArrayList<>();
    private final List<Long> postIds = new ArrayList<>();

    @BeforeEach
    public void initMainMember(){
        runInTx(() -> {
            Member member = Member.builder()
                    .email("test1@test.com")
                    .nickName(INITIAL_NAME)
                    .balance(INITIAL_BALANCE)
                    .address(ADDRESS)
                    .build();

            em.persist(member);
            em.flush();
            mainMemberId = member.getId();
            em.clear();
        });
    }

    @AfterEach
    public void clear() {
        runInTx(() -> {
            em.createQuery("delete from PaymentHistory").executeUpdate();
            em.createQuery("delete from Post").executeUpdate();
            em.createQuery("delete from Member").executeUpdate();
        });
    }

    @Nested
    @DisplayName("동시 판매")
    class MultiBuyersToSingleSeller {

        /**
         * INITIAL_BALANCE 포함 모든 POST_PRICE의 값이 더해짐
         * 따라서 모든 로직이 실행된 후 잔액은 INITIAL_BALANCE + (POST_PRICE * 100)이 된다.
         */
        @Test
        @DisplayName("[동시성] n명이 한명의 게시물 n건 구매시 판매자의 잔액 검증")
        public void confirmPayment_MultiBuyersSingleSeller_Success() throws Exception {
            //given
            int buyUserCount = 100;

            List<Long> sellers = Collections.nCopies(100, mainMemberId);
            initPosts(sellers);
            initCounterpartMembers(buyUserCount);

            ExecutorService executor = Executors.newFixedThreadPool(buyUserCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(buyUserCount);

            for (int i = 0; i < buyUserCount; i++) {
                final int index = i;
                String formattedIndex = String.format("%02d", index);
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        paymentService.confirmPayment(counterpartMemberIds.get(index), new PaymentTokenDto("TXN_" + formattedIndex, POST_PRICE, postIds.get(index)));
                    } catch (Exception e) {
                        System.out.println("e = " + e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            //when
            startLatch.countDown(); //동시 실행 시작
            boolean completed = doneLatch.await(10, TimeUnit.SECONDS); // 모든 작업 종료 대기
            executor.shutdown(); //executor 종료
            //then
            assertThat(completed).as("동시성 작업 타임아웃 - 데드락 의심").isTrue();
            em.clear();
            Member toMember = em.find(Member.class, mainMemberId);
            assertThat(toMember.getBalance()).isEqualTo(INITIAL_BALANCE + POST_PRICE * buyUserCount);

            List<PaymentHistory> paymentHistoryList = em.createQuery("select p from PaymentHistory p where p.type = :type and p.member.id = :toMemberId", PaymentHistory.class)
                    .setParameter("type", PaymentStatus.DEPOSIT)
                    .setParameter("toMemberId", mainMemberId)
                    .getResultList();

            assertThat(paymentHistoryList).hasSize(buyUserCount);
            Long totalAmount = 0L;

            for (PaymentHistory paymentHistory : paymentHistoryList) {
                totalAmount += paymentHistory.getChangedCash();
            }

            assertThat(totalAmount).isEqualTo(toMember.getBalance() - INITIAL_BALANCE);

            List<Post> statusList = em.createQuery("select p from Post p where p.status = :status", Post.class)
                    .setParameter("status", PostStatus.SOLD)
                    .getResultList();

            assertThat(statusList).hasSize(buyUserCount);
        }
    }

    @Nested
    @DisplayName("동시 구매")
    class SingleBuyerToMultiSellers {

        private static final Long BUYER_INITIAL_BALANCE = 500_000L;

        /**
         * 구매자(mainMember)의 잔액을 BUYER_INITIAL_BALANCE를 통해 재설정 후 검증
         * 따라서 모든 로직이 끝나면 INITIAL_BALANCE만 남도록 의도
         */
        @Test
        @DisplayName("[동시성] 한명이 n명의 게시물 n건 구매시 구매자의 잔액 검증")
        public void confirmPayment_SingleBuyerMultiSellers_Success() throws Exception {
            //given
            int buyUserCount = 100;

            initCounterpartMembers(buyUserCount);
            initPosts(counterpartMemberIds);
            settingMemberBalance();

            ExecutorService executor = Executors.newFixedThreadPool(buyUserCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(buyUserCount);
            for (int i = 0; i < buyUserCount; i++) {
                final int index = i;
                String formattedIndex = String.format("%02d", index);
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        paymentService.confirmPayment(mainMemberId, new PaymentTokenDto("TXN_" + formattedIndex, POST_PRICE, postIds.get(index)));
                    } catch (Exception e) {
                        System.out.println("e = " + e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            //when
            startLatch.countDown(); //동시 실행 시작
            boolean completed = doneLatch.await(10, TimeUnit.SECONDS); // 모든 작업 종료 대기
            executor.shutdown(); //executor 종료
            //then
            assertThat(completed).as("동시성 작업 타임아웃 - 데드락 의심");
            em.clear();
            Member toMember = em.find(Member.class, mainMemberId);
            assertThat(toMember.getBalance()).isEqualTo(INITIAL_BALANCE);

            List<PaymentHistory> paymentHistoryList = em.createQuery("select p from PaymentHistory p where p.type = :type and p.member.id = :toMemberId", PaymentHistory.class)
                    .setParameter("type", PaymentStatus.WITHDRAWAL)
                    .setParameter("toMemberId", mainMemberId)
                    .getResultList();

            assertThat(paymentHistoryList).hasSize(buyUserCount);
            Long totalAmount = 0L;

            for (PaymentHistory paymentHistory : paymentHistoryList) {
                totalAmount += paymentHistory.getChangedCash();
            }

            assertThat(totalAmount).isEqualTo(BUYER_INITIAL_BALANCE);

            List<Post> statusList = em.createQuery("select p from Post p where p.status = :status", Post.class)
                    .setParameter("status", PostStatus.SOLD)
                    .getResultList();

            assertThat(statusList).hasSize(buyUserCount);
        }

        private void settingMemberBalance() {
            runInTx(() -> {
                Member member = em.find(Member.class, mainMemberId);
                member.changeBalance(true, BUYER_INITIAL_BALANCE);
            });
        }
    }

    @Nested
    @DisplayName("잔액 부족 동시 결제")
    class InsufficientBalanceOnConcurrentPayment {

        /**
         * 의도적으로 INITIAL_BALANCE와 POST_PRICE가 같도록 함
         * 따라서 한건만 결제가 되고, 다른 한건은 잔액 부족에 대한 검증
         */
        @Test
        @DisplayName("[동시성] 잔액 부족 시 1건만 성공하고 나머지는 잔액부족 예외 발생")
        public void confirmPayment_InsufficientBalance_PartialSuccess() throws Exception {
            //given
            int buyUserCount = 2;

            initCounterpartMembers(buyUserCount);
            initPosts(counterpartMemberIds);

            ExecutorService executor = Executors.newFixedThreadPool(buyUserCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(buyUserCount);
            AtomicInteger successCount = new AtomicInteger();
            AtomicInteger failCount = new AtomicInteger();
            ConcurrentLinkedQueue<Object> unexpectedErrors = new ConcurrentLinkedQueue<>();

            for (int i = 0; i < buyUserCount; i++) {
                final int index = i;
                String formattedIndex = String.format("%02d", index);
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        paymentService.confirmPayment(mainMemberId, new PaymentTokenDto("TXN_" + formattedIndex, POST_PRICE, postIds.get(index)));
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        if (!"잔액이 부족합니다.".equals(e.getMessage())) unexpectedErrors.add(e);
                        failCount.incrementAndGet();
                        System.out.println("e = " + e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            //when
            startLatch.countDown(); //동시 실행 시작
            boolean completed = doneLatch.await(10, TimeUnit.SECONDS); // 모든 작업 종료 대기
            executor.shutdown(); //executor 종료
            //then
            assertThat(completed).as("동시성 작업 타임아웃 - 데드락 의심");
            assertThat(unexpectedErrors).isEmpty();
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failCount.get()).isEqualTo(1);

            em.clear();
            Member member = em.find(Member.class, mainMemberId);
            assertThat(member.getBalance()).isEqualTo(0L);

            List<PaymentHistory> paymentHistoryList = em.createQuery("select p from PaymentHistory p order by p.id asc", PaymentHistory.class)
                    .getResultList();

            assertThat(paymentHistoryList).hasSize(2);

            List<Post> soldPosts = em.createQuery("select p from Post p where p.status = :status", Post.class)
                    .setParameter("status", PostStatus.SOLD)
                    .getResultList();

            assertThat(soldPosts).hasSize(1);
        }
    }

    @Nested
    @DisplayName("동일 결제 링크 중복 결제 확인")
    class SameBuyerWithSamePaymentToken {

        private static final Long BUYER_INITIAL_BALANCE = 500_000L;

        @Test
        @DisplayName("[멱등성] 같은 거래 번호로 결제 진행시 중복 확인")
        public void confirmPayment_DuplicateTokenIdempotency_PartialSuccess() throws Exception {
            //given
            int buyUserCount = 2;

            settingMemberBalance();

            initCounterpartMembers(buyUserCount);
            initPosts(counterpartMemberIds);

            ExecutorService executor = Executors.newFixedThreadPool(buyUserCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(buyUserCount);
            AtomicInteger successCount = new AtomicInteger();
            AtomicInteger failCount = new AtomicInteger();
            ConcurrentLinkedQueue<Object> unexpectedErrors = new ConcurrentLinkedQueue<>();

            for (int i = 0; i < buyUserCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        paymentService.confirmPayment(mainMemberId, new PaymentTokenDto("TXN_11" , POST_PRICE, postIds.get(1))); //동일한 tranSeqNo
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        boolean isExpected = e instanceof DataIntegrityViolationException
                                        || (e instanceof InvalidInputException &&
                                        ("이미 진행된 거래입니다.".equals(e.getMessage()) || "이미 판매된 제품입니다.".equals(e.getMessage()))
                        );
                        if (!isExpected) unexpectedErrors.add(e);
                        failCount.incrementAndGet();
                        System.out.println("e = " + e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            //when
            startLatch.countDown(); //동시 실행 시작
            boolean completed = doneLatch.await(10, TimeUnit.SECONDS); // 모든 작업 종료 대기
            executor.shutdown(); //executor 종료
            //then
            assertThat(completed).as("동시성 작업 타임아웃 - 데드락 의심");
            assertThat(unexpectedErrors).isEmpty();
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failCount.get()).isEqualTo(1);

            em.clear();
            Member member = em.find(Member.class, mainMemberId);
            assertThat(member.getBalance()).isEqualTo(BUYER_INITIAL_BALANCE); // 기존(INITIAL_BALANCE) + 추가 (BUYER_INITIAL_BALANCE) == 505_000L 따라서 결제 후 잔고 : 500_000L

            List<PaymentHistory> paymentHistoryList = em.createQuery("select p from PaymentHistory p order by p.id asc", PaymentHistory.class)
                    .getResultList();
            assertThat(paymentHistoryList).hasSize(2);

            List<Post> soldPosts = em.createQuery("select p from Post p where p.status = :status", Post.class)
                    .setParameter("status", PostStatus.SOLD)
                    .getResultList();
            assertThat(soldPosts).hasSize(1);
        }

        private void settingMemberBalance() {
            runInTx(() -> {
                Member member = em.find(Member.class, mainMemberId);
                member.changeBalance(true, BUYER_INITIAL_BALANCE);
            });
        }
    }

    @Nested
    @DisplayName("상호 결제 시 데드락 검증")
    class MutualPaymentBetweenTwoMembers {

        @Test
        @DisplayName("[동시성] A와 B가 서로의 게시물을 동시 결제 시 데드락 없이 정상 처리")
        public void confirmPayment_MutualPayment_NoDeadlock() throws Exception {
            //given
            int buyUserCount = 1;

            initCounterpartMembers(buyUserCount);
            Long memberAId = mainMemberId;
            Long memberBId = counterpartMemberIds.getFirst();

            initPosts(List.of(memberAId,memberBId));
            Long postA = postIds.getFirst();
            Long postB = postIds.getLast();

            ExecutorService executor = Executors.newFixedThreadPool(2);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(2);

            executor.submit(() -> { //A -> B
                try {
                    startLatch.await();
                    paymentService.confirmPayment(memberAId, new PaymentTokenDto("TXN_A_TO_B" , POST_PRICE, postB));
                } catch (Exception e) {
                    System.out.println("e = " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });

            executor.submit(() -> { // B -> A
                try {
                    startLatch.await();
                    paymentService.confirmPayment(memberBId, new PaymentTokenDto("TXN_B_TO_A" , POST_PRICE, postA));
                } catch (Exception e) {
                    System.out.println("e = " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });

            //when
            startLatch.countDown(); //동시 실행 시작
            boolean completed = doneLatch.await(10, TimeUnit.SECONDS); // 모든 작업 종료 대기
            executor.shutdown(); //executor 종료

            //then
            assertThat(completed).as("동시성 작업 타임아웃 - 데드락 의심");
            em.clear();
            Member memberA = em.find(Member.class, memberAId);
            Member memberB = em.find(Member.class, memberBId);

            assertThat(memberA.getBalance()).isEqualTo(INITIAL_BALANCE);
            assertThat(memberB.getBalance()).isEqualTo(INITIAL_BALANCE);

            List<PaymentHistory> histories = em.createQuery("select p from PaymentHistory p", PaymentHistory.class).getResultList();
            assertThat(histories).hasSize(4);

            List<Post> soldPosts = em.createQuery("select p from Post p where p.status = :status", Post.class)
                    .setParameter("status", PostStatus.SOLD)
                    .getResultList();
            assertThat(soldPosts).hasSize(2);
        }
    }

    @Nested
    @DisplayName("동일 게시물 동시 결제 시 이중 판매 방지")
    class PreventDoubleSaleOnSamePost {

        @Test
        @DisplayName("[동시성] 두 명이 같은 게시물을 동시 결제 시 1명만 성공")
        public void confirmPayment_DoubleSaleOnSamePost_OnlyOneSucceeds() throws Exception {
            //given
            int buyUserCount = 2;
            initCounterpartMembers(buyUserCount);

            initPosts(List.of(mainMemberId));
            Long sharedPostId = postIds.getFirst();

            ExecutorService executor = Executors.newFixedThreadPool(buyUserCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(buyUserCount);

            for (int i = 0; i < buyUserCount; i++) {
                final int index = i;
                String formattedIndex = String.format("%02d", index);
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        paymentService.confirmPayment(counterpartMemberIds.get(index), new PaymentTokenDto("TXN_" + formattedIndex, POST_PRICE, sharedPostId));
                    } catch (Exception e) {
                        System.out.println("e = " + e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            //when
            startLatch.countDown();
            boolean completed = doneLatch.await(10, TimeUnit.SECONDS); // 모든 작업 종료 대기
            executor.shutdown(); //executor 종료

            //then
            assertThat(completed).as("동시성 작업 타임아웃 - 데드락 의심");
            em.clear();
            List<Post> soldPosts = em.createQuery("select p from Post p where p.status = :status", Post.class)
                    .setParameter("status", PostStatus.SOLD)
                    .getResultList();
            assertThat(soldPosts).hasSize(1);

            Member seller = em.find(Member.class, mainMemberId);
            assertThat(seller.getBalance()).isEqualTo(INITIAL_BALANCE + POST_PRICE);

            Member buyer1 = em.find(Member.class, counterpartMemberIds.getFirst());
            Member buyer2 = em.find(Member.class, counterpartMemberIds.getLast());

            assertThat((buyer1.getBalance().equals(INITIAL_BALANCE - POST_PRICE) && buyer2.getBalance().equals(INITIAL_BALANCE)) ||
                    (buyer2.getBalance().equals(INITIAL_BALANCE - POST_PRICE) && buyer1.getBalance().equals(INITIAL_BALANCE))
            ).isTrue();

            List<PaymentHistory> histories = em.createQuery("select p from PaymentHistory p", PaymentHistory.class)
                    .getResultList();
            assertThat(histories).hasSize(2);
        }
    }

    private void initCounterpartMembers(int userCount) {
        runInTx(() -> {
            for (int i = 0; i < userCount; i++) {
                Member member = Member.builder()
                        .email("test" + i + "@test.com")
                        .nickName(INITIAL_NAME)
                        .balance(INITIAL_BALANCE)
                        .address(ADDRESS)
                        .build();

                em.persist(member);
                counterpartMemberIds.add(member.getId());
            }
            em.flush();
            em.clear();
        });
    }

    private void initPosts(List<Long> sellerIds) {
        runInTx(() -> {
            for (Long sellerId : sellerIds) {
                Member seller = em.find(Member.class, sellerId);
                Post targetPost = new Post(seller, "제목", "내용", POST_PRICE, ADDRESS);
                em.persist(targetPost);
                postIds.add(targetPost.getId());
            }
            em.flush();
            em.clear();
        });
    }

    /**
     * Spring Data JPA 사용하지 않아 직접 트랜잭션 관리를 해줘야함
     * @param action
     * 공통된 트랜잭션 관리를 진행하기 위해 runInTx메서드를 람다식을 사용하여 관리한다.
     */
    private void runInTx(Runnable action) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            action.run();
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
    }
}
