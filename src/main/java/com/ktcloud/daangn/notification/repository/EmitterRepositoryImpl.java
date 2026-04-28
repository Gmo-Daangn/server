package com.ktcloud.daangn.notification.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter save(Long memberId, SseEmitter emitter) {
        emitters.put(memberId, emitter);
        return emitter;
    }

    @Override
    public void deleteById(Long memberId) {
        emitters.remove(memberId);
    }

    @Override
    public SseEmitter get(Long memberId) {
        return emitters.get(memberId);
    }
}
