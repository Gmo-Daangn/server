package com.ktcloud.daangn.notification.redis;

import com.ktcloud.daangn.notification.config.NotificationRedisProperties;
import com.ktcloud.daangn.notification.dto.NotificationSseMessage;
import com.ktcloud.daangn.notification.service.NotificationSseDeliveryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationSseRedisSubscriberTest {

    static final Long MEMBER_ID = 1L;

    @Mock
    ObjectMapper objectMapper;
    @Mock
    NotificationSseDeliveryService deliveryService;

    @InjectMocks
    NotificationSseRedisSubscriber subscriber;

    @Test
    @DisplayName("[HAPPY] Redis pub/sub 메시지를 수신하면 로컬 SSE로 전달한다")
    void onMessage_validPayload_deliversToLocalSse() throws Exception {
        String payload = "{\"receiverId\":1,\"message\":\"새 알림\"}";
        givenObjectMapperReturns(payload);

        subscriber.onMessage(new DefaultMessage("notification:sse".getBytes(), payload.getBytes()), null);

        verify(deliveryService).deliver(MEMBER_ID, "새 알림");
    }

    private void givenObjectMapperReturns(String payload) throws Exception {
        org.mockito.BDDMockito.given(objectMapper.readValue(payload, com.ktcloud.daangn.notification.dto.NotificationSseMessage.class))
                .willReturn(new com.ktcloud.daangn.notification.dto.NotificationSseMessage(MEMBER_ID, "새 알림"));
    }
}
