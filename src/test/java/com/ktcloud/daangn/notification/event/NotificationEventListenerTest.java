package com.ktcloud.daangn.notification.event;

import com.ktcloud.daangn.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    NotificationService notificationService;

    @InjectMocks
    NotificationEventListener notificationEventListener;

    @Test
    @DisplayName("[HAPPY] 이벤트를 수신하면 NotificationService.createAndSendNotification을 호출한다")
    void handleNotificationEvent_callsService() {
        NotificationEvent event = new NotificationEvent(1L, "ORDER", 10L, "주문 완료");

        notificationEventListener.handleNotificationEvent(event);

        verify(notificationService, times(1)).createAndSendNotification(event);
    }

    @Test
    @DisplayName("[HAPPY] 다른 이벤트 데이터로도 서비스에 그대로 위임한다")
    void handleNotificationEvent_differentEvent_delegatesToService() {
        NotificationEvent event = new NotificationEvent(99L, "CHAT", 55L, "새 메시지가 왔습니다");

        notificationEventListener.handleNotificationEvent(event);

        verify(notificationService).createAndSendNotification(event);
    }

    @Test
    @DisplayName("[HAPPY] 이벤트 레코드 필드 값이 서비스에 전달된 이벤트와 동일하다")
    void handleNotificationEvent_passesSameEventInstance() {
        NotificationEvent event = new NotificationEvent(2L, "REVIEW", 7L, "리뷰 요청");

        notificationEventListener.handleNotificationEvent(event);

        // 동일한 event 객체가 서비스로 전달되었는지 검증
        verify(notificationService).createAndSendNotification(event);
    }
}