package com.ktcloud.daangn.chat.event;

import java.util.List;

// 채팅 저장 후 발생하는 이벤트 레코드
public record ChatMessageSentEvent(
        Long chatRoomId, 
        Long senderId, 
        Long messageId, 
        String messagePreview, 
        List<Long> receiverIds
) {
}
