package com.ktcloud.daangn.notification.event;

import com.ktcloud.daangn.notification.config.NotificationTemplateTypes;
import com.ktcloud.daangn.payment.event.DepositCompleteEvent;
import com.ktcloud.daangn.payment.event.PaymentCompleteEvent;
import com.ktcloud.daangn.payment.event.PaymentRequestEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationBridgeTest {

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    PaymentNotificationBridge bridge;

    @Test
    @DisplayName("[HAPPY] 충전 완료 이벤트를 알림 이벤트로 변환한다")
    void onDepositComplete_publishesNotificationEvent() {
        DepositCompleteEvent depositEvent = new DepositCompleteEvent(1L, 10_000L);

        bridge.onDepositComplete(depositEvent);

        NotificationEvent published = capturePublishedEvent();
        assertThat(published.receiverId()).isEqualTo(1L);
        assertThat(published.templateType()).isEqualTo(NotificationTemplateTypes.DEPOSIT_COMPLETE);
        assertThat(published.identifier()).isZero();
        assertThat(published.templateText()).isEqualTo("10,000");
    }

    @Test
    @DisplayName("[HAPPY] 결제 요청 이벤트를 알림 이벤트로 변환한다")
    void onPaymentRequest_publishesNotificationEvent() {
        PaymentRequestEvent paymentRequestEvent = new PaymentRequestEvent(2L, 10_000L, 10L);

        bridge.onPaymentRequest(paymentRequestEvent);

        NotificationEvent published = capturePublishedEvent();
        assertThat(published.receiverId()).isEqualTo(2L);
        assertThat(published.templateType()).isEqualTo(NotificationTemplateTypes.PAYMENT_REQUEST);
        assertThat(published.identifier()).isEqualTo(10L);
        assertThat(published.templateText()).isEqualTo("10,000");
    }

    @Test
    @DisplayName("[HAPPY] 결제 완료 이벤트를 알림 이벤트로 변환한다")
    void onPaymentComplete_publishesNotificationEvent() {
        PaymentCompleteEvent paymentCompleteEvent = new PaymentCompleteEvent(3L, 10_000L, 20L);

        bridge.onPaymentComplete(paymentCompleteEvent);

        NotificationEvent published = capturePublishedEvent();
        assertThat(published.receiverId()).isEqualTo(3L);
        assertThat(published.templateType()).isEqualTo(NotificationTemplateTypes.PAYMENT_COMPLETE);
        assertThat(published.identifier()).isEqualTo(20L);
        assertThat(published.templateText()).isEqualTo("10,000");
    }

    private NotificationEvent capturePublishedEvent() {
        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }
}
