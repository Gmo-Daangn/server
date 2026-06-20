package com.ktcloud.daangn.notification.redis;

import com.ktcloud.daangn.notification.dto.NotificationSseMessage;
import com.ktcloud.daangn.notification.service.NotificationSseDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class NotificationSseRedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final NotificationSseDeliveryService deliveryService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            NotificationSseMessage sseMessage = objectMapper.readValue(body, NotificationSseMessage.class);
            deliveryService.deliver(sseMessage.receiverId(), sseMessage.message());
        } catch (Exception exception) {
            log.error("Redis pub/sub 알림 메시지 처리 실패", exception);
        }
    }
}
