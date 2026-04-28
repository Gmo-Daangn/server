package com.ktcloud.daangn.notification.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EmitterRepository {

    SseEmitter save(Long memberId, SseEmitter emitter);

    void deleteById(Long memberId);

    SseEmitter get(Long memberId);
}
