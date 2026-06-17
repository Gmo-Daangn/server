package com.ktcloud.daangn.notification.event;

import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.payment.dto.PaymentInitRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentTokenDto;
import com.ktcloud.daangn.payment.event.DepositCompleteEvent;
import com.ktcloud.daangn.payment.event.PaymentCompleteEvent;
import com.ktcloud.daangn.payment.event.PaymentRequestEvent;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventPublishingAspectTest {

    private static final Long SELLER_ID = 1L;
    private static final Long BUYER_ID = 2L;
    private static final Long POST_ID = 1L;
    private static final Long ROOM_ID = 1L;
    private static final Long AMOUNT = 1L;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Mock
    PaymentNotificationContextResolver contextResolver;

    @Mock
    PostService postService;

    @InjectMocks
    PaymentEventPublishingAspect aspect;

    Post post;

    @BeforeEach
    void setUp() {
        post = Post.builder()
                .member(com.ktcloud.daangn.member.entity.Member.builder().id(SELLER_ID).build())
                .title("title")
                .content("content")
                .price(AMOUNT)
                .location(new Address("서울시", "강남구", "청담동"))
                .build();
        ReflectionTestUtils.setField(post, "id", POST_ID);
    }

    @Nested
    @DisplayName("publishDepositCompleteEvent")
    class Deposit {

        @Test
        @DisplayName("[HAPPY] 충전 완료 이벤트를 발행한다")
        void publishesDepositCompleteEvent() {
            PaymentRequestDto dto = new PaymentRequestDto("tran-1", AMOUNT, SELLER_ID);

            aspect.publishDepositCompleteEvent(dto);

            ArgumentCaptor<DepositCompleteEvent> captor = ArgumentCaptor.forClass(DepositCompleteEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().memberId()).isEqualTo(SELLER_ID);
            assertThat(captor.getValue().amount()).isEqualTo(AMOUNT);
        }
    }

    @Nested
    @DisplayName("publishPaymentRequestEvents")
    class RequestPayment {

        @Test
        @DisplayName("[HAPPY] 결제 요청 대상에게 이벤트를 발행한다")
        void publishesPaymentRequestEvents() {
            PaymentInitRequestDto dto = new PaymentInitRequestDto(ROOM_ID, POST_ID, AMOUNT);
            given(contextResolver.resolvePaymentRequestTargets(POST_ID))
                    .willReturn(List.of(new PaymentNotificationContextResolver.PaymentRequestTarget(BUYER_ID, ROOM_ID)));

            aspect.publishPaymentRequestEvents(dto);

            ArgumentCaptor<PaymentRequestEvent> captor = ArgumentCaptor.forClass(PaymentRequestEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            PaymentRequestEvent event = captor.getValue();
            assertThat(event.targetMemberId()).isEqualTo(BUYER_ID);
            assertThat(event.amount()).isEqualTo(AMOUNT);
            assertThat(event.roomId()).isEqualTo(ROOM_ID);
        }

        @Test
        @DisplayName("[HAPPY] 결제 요청 대상이 없으면 이벤트를 발행하지 않는다")
        void doesNotPublishWhenTargetNotFound() {
            PaymentInitRequestDto dto = new PaymentInitRequestDto(ROOM_ID, POST_ID, AMOUNT);
            given(contextResolver.resolvePaymentRequestTargets(POST_ID)).willReturn(List.of());

            aspect.publishPaymentRequestEvents(dto);

            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("publishPaymentCompleteEvent")
    class ConfirmPayment {

        @Test
        @DisplayName("[HAPPY] 결제 완료 이벤트를 발행한다")
        void publishesPaymentCompleteEvent() {
            PaymentTokenDto dto = new PaymentTokenDto("tran-2", AMOUNT, POST_ID);
            given(postService.getPostOrThrow(POST_ID)).willReturn(post);
            given(contextResolver.resolveChatRoomId(SELLER_ID, BUYER_ID, POST_ID))
                    .willReturn(Optional.of(ROOM_ID));

            aspect.publishPaymentCompleteEvent(BUYER_ID, dto);

            ArgumentCaptor<PaymentCompleteEvent> captor = ArgumentCaptor.forClass(PaymentCompleteEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            PaymentCompleteEvent event = captor.getValue();
            assertThat(event.sellerId()).isEqualTo(SELLER_ID);
            assertThat(event.amount()).isEqualTo(AMOUNT);
            assertThat(event.roomId()).isEqualTo(ROOM_ID);
        }

        @Test
        @DisplayName("[HAPPY] 채팅방을 찾지 못하면 이벤트를 발행하지 않는다")
        void doesNotPublishWhenChatRoomNotFound() {
            PaymentTokenDto dto = new PaymentTokenDto("tran-2", AMOUNT, POST_ID);
            given(postService.getPostOrThrow(POST_ID)).willReturn(post);
            given(contextResolver.resolveChatRoomId(SELLER_ID, BUYER_ID, POST_ID))
                    .willReturn(Optional.empty());

            aspect.publishPaymentCompleteEvent(BUYER_ID, dto);

            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}
