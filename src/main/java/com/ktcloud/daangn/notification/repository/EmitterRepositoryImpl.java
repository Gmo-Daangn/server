package com.ktcloud.daangn.notification.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter save(Long receiverId, SseEmitter emitter) {
        emitters.put(receiverId, emitter);
        return emitter;
    }

    @Override
    public void deleteById(Long receiverId) {
        emitters.remove(receiverId);
    }

    @Override
    public SseEmitter get(Long receiverId) {
        return emitters.get(receiverId);
    }
}
