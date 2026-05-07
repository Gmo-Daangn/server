package com.ktcloud.daangn.notification.event;

import com.ktcloud.daangn.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {
    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    @Async
    public void handleNotificationEvent(NotificationEvent event) {
        notificationService.createAndSendNotification(event);
    }
}
