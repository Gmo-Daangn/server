package com.ktcloud.daangn.notification.redis;

import com.ktcloud.daangn.notification.service.NotificationSseDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@RequiredArgsConstructor
public class LocalNotificationSsePublisher implements NotificationSsePublisher {

    private final NotificationSseDeliveryService deliveryService;

    @Override
    public void publish(Long receiverId, String message) {
        deliveryService.deliver(receiverId, message);
    }
}
