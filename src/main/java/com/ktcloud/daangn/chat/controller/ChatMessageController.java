package com.ktcloud.daangn.chat.controller;

import com.ktcloud.daangn.chat.dto.ChatMessageRequestDto;
import com.ktcloud.daangn.chat.service.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatServiceImpl chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/rooms/{roomId}/messages")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageRequestDto dto) {
        // 비지니스 로직 처리
        chatService.SendChat(dto);
        
        // 메시지 전송(sub)
        messagingTemplate.convertAndSend("/sub/rooms/" + roomId + "/messages", dto.message());
    }

    @GetMapping
    public void findMessages() {

    }
}
