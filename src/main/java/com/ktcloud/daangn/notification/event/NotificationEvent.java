package com.ktcloud.daangn.notification.event;

public record NotificationEvent(
        Long memberId,
        String templateType,
        Long identifier,
        String templateText
) {
}
