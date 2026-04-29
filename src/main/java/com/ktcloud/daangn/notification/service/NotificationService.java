package com.ktcloud.daangn.notification.service;

import com.ktcloud.daangn.notification.dto.NotificationResponseDto;
import com.ktcloud.daangn.notification.event.NotificationEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
public interface NotificationService {

    SseEmitter subscribe(Long memberId);

    void createAndSendNotification(NotificationEvent event);

    List<NotificationResponseDto> getNotifications(Long memberId);

    String deleteNotification(Long id);

    String readNotification(Long id);
}