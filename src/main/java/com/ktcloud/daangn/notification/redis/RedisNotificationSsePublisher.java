package com.ktcloud.daangn.notification.redis;

import com.ktcloud.daangn.notification.config.NotificationRedisProperties;
import com.ktcloud.daangn.notification.dto.NotificationSseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class RedisNotificationSsePublisher implements NotificationSsePublisher {

    private final StringRedisTemplate redisTemplate;
    private final NotificationRedisProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(Long receiverId, String message) {
        try {
            String payload = objectMapper.writeValueAsString(new NotificationSseMessage(receiverId, message));
            redisTemplate.convertAndSend(properties.pubsubChannel(), payload);
        } catch (Exception exception) {
            log.error("Redis pub/sub 알림 발행 실패 [receiverId={}]", receiverId, exception);
        }
    }
}
