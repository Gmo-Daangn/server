package com.ktcloud.daangn.notification.event;

import com.ktcloud.daangn.chat.event.ChatMessageSentEvent;
import com.ktcloud.daangn.notification.config.NotificationTemplateTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatMessageSentNotificationBridgeTest {

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    ChatMessageSentNotificationBridge bridge;

    @Test
    @DisplayName("[HAPPY] 수신자 수만큼 NotificationEvent를 발행한다")
    void onChatMessageSent_publishesOneNotificationPerReceiver() {
        ChatMessageSentEvent chatEvent = new ChatMessageSentEvent(
                10L,
                1L,
                99L,
                "미리보기",
                List.of(2L, 3L));

        bridge.onChatMessageSent(chatEvent);

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        List<NotificationEvent> published = captor.getAllValues();
        assertThat(published).extracting(NotificationEvent::receiverId).containsExactly(2L, 3L);
        assertThat(published).extracting(NotificationEvent::templateType).containsOnly(NotificationTemplateTypes.CHAT);
        assertThat(published).extracting(NotificationEvent::identifier).containsOnly(10L);
        assertThat(published).extracting(NotificationEvent::templateText).containsOnly("미리보기");
    }

    @Test
    @DisplayName("[HAPPY] 수신자 목록이 비어 있으면 발행하지 않는다")
    void onChatMessageSent_emptyReceivers_doesNotPublish() {
        ChatMessageSentEvent chatEvent = new ChatMessageSentEvent(10L, 1L, 99L, "x", List.of());

        bridge.onChatMessageSent(chatEvent);

        verify(eventPublisher, never()).publishEvent(any());
    }
}
