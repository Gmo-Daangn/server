package com.ktcloud.daangn.notification.redis;

import com.ktcloud.daangn.notification.config.NotificationRedisProperties;
import com.ktcloud.daangn.notification.dto.NotificationSseMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisNotificationSsePublisherTest {

    static final Long MEMBER_ID = 1L;
    static final String CHANNEL = "notification:sse";

    @Mock
    StringRedisTemplate redisTemplate;
    @Mock
    NotificationRedisProperties properties;
    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    RedisNotificationSsePublisher publisher;

    @Test
    @DisplayName("[HAPPY] 알림 메시지를 Redis pub/sub 채널로 발행한다")
    void publish_validMessage_convertsAndSends() throws Exception {
        given(properties.pubsubChannel()).willReturn(CHANNEL);
        given(objectMapper.writeValueAsString(new NotificationSseMessage(MEMBER_ID, "새 알림")))
                .willReturn("{\"receiverId\":1,\"message\":\"새 알림\"}");

        publisher.publish(MEMBER_ID, "새 알림");

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).convertAndSend(eq(CHANNEL), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).contains("새 알림");
    }
}
