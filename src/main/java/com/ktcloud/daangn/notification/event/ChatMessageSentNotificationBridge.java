package com.ktcloud.daangn.notification.event;

import com.ktcloud.daangn.chat.event.ChatMessageSentEvent;
import com.ktcloud.daangn.notification.config.NotificationTemplateTypes;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 채팅 이벤트를 알림으로 전달
@Component
@RequiredArgsConstructor
public class ChatMessageSentNotificationBridge {

    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onChatMessageSent(ChatMessageSentEvent event) {
        for (Long receiverId : event.receiverIds()) {
            eventPublisher.publishEvent(new NotificationEvent(
                    receiverId,
                    NotificationTemplateTypes.CHAT,
                    event.chatRoomId(),
                    event.messagePreview()));
        }
    }
}
