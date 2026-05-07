package com.ktcloud.daangn.chat.controller;

import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.dto.ChatMessageWriteRequestDto;
import com.ktcloud.daangn.chat.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅 메시지 전송 처리
    @MessageMapping("/chat/rooms/{roomId}/messages")
    public void create(@DestinationVariable Long roomId, @Valid @Payload ChatMessageWriteRequestDto dto) {
        ChatMessageResponseDto response = chatMessageService.create(roomId, dto.memberId(), dto.message());
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + roomId + "/messages", response);
    }
}
