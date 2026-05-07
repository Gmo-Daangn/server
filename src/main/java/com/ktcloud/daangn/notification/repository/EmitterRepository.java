package com.ktcloud.daangn.notification.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EmitterRepository {

    SseEmitter save(Long receiverId, SseEmitter emitter);

    void deleteById(Long receiverId);

    SseEmitter get(Long receiverId);
}
