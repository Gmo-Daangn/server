package com.ktcloud.daangn.notification.redis;

public interface NotificationSsePublisher {

    void publish(Long receiverId, String message);
}
