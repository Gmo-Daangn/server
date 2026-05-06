package com.ktcloud.daangn.notification.controller;

import com.ktcloud.daangn.config.dto.BaseResponse;
import com.ktcloud.daangn.notification.dto.NotificationResponseDto;
import com.ktcloud.daangn.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam("memberId") Long receiverId) {
        return notificationService.subscribe(receiverId);
    }

    // 알림 목록 조회
    @GetMapping
    public BaseResponse<List<NotificationResponseDto>> getNotifications(@RequestParam("memberId") Long receiverId) {
        return BaseResponse.success(notificationService.getNotifications(receiverId));
    }

    // 알림 삭제
    @DeleteMapping("/{id}")
    public BaseResponse<String> deleteNotification(@PathVariable("id") Long id) {
        return BaseResponse.success(notificationService.deleteNotification(id));
    }

    // 알림 읽음처리
    @PatchMapping("/{id}")
    public BaseResponse<String> readNotification(@PathVariable("id") Long id) {
        return BaseResponse.success(notificationService.readNotification(id));
    }
}