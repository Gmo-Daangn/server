package com.ktcloud.daangn.notification.dto;

import com.ktcloud.daangn.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        Long id,
        Long memberId,
        String templateType,
        String templateTitle,
        Long identifier,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponseDto from(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getMemberId(),
                notification.getTemplate().getTemplateType(),
                notification.getTemplate().getTemplateTitle(),
                notification.getTemplate().getIdentifier(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
