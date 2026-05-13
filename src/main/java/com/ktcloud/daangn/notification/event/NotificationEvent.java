package com.ktcloud.daangn.notification.event;

public record NotificationEvent(
        Long receiverId,
        String templateType,
        Long identifier,
        String templateText
) {
}
