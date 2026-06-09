package com.ktcloud.daangn.notification.event;

import com.ktcloud.daangn.notification.config.NotificationTemplateTypes;
import com.ktcloud.daangn.payment.event.DepositCompleteEvent;
import com.ktcloud.daangn.payment.event.PaymentCompleteEvent;
import com.ktcloud.daangn.payment.event.PaymentRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentNotificationBridge {

    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onDepositComplete(DepositCompleteEvent event) {
        eventPublisher.publishEvent(new NotificationEvent(
                event.memberId(),
                NotificationTemplateTypes.DEPOSIT_COMPLETE,
                0L,
                String.format("%,d", event.amount())
        ));
    }

    @EventListener
    public void onPaymentRequest(PaymentRequestEvent event) {
        eventPublisher.publishEvent(new NotificationEvent(
                event.targetMemberId(),
                NotificationTemplateTypes.PAYMENT_REQUEST,
                event.roomId(),
                String.format("%,d", event.amount())
        ));
    }

    @EventListener
    public void onPaymentComplete(PaymentCompleteEvent event) {
        eventPublisher.publishEvent(new NotificationEvent(
                event.sellerId(),
                NotificationTemplateTypes.PAYMENT_COMPLETE,
                event.roomId(),
                String.format("%,d", event.amount())
        ));
    }
}
