package com.ktcloud.daangn.notification.service;

import com.ktcloud.daangn.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSseDeliveryService {

    private final EmitterRepository emitterRepository;

    public void deliver(Long receiverId, String message) {
        SseEmitter emitter = emitterRepository.get(receiverId);
        if (emitter == null) {
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(System.currentTimeMillis()))
                    .name("notification")
                    .data(message));
        } catch (IOException exception) {
            emitterRepository.deleteById(receiverId);
            log.error("SSE 전송 실패로 인한 연결 삭제: {}", receiverId);
        }
    }
}
