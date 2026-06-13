package com.ktcloud.daangn.chat.controller;

import com.ktcloud.daangn.auth.dto.CustomUser;
import com.ktcloud.daangn.chat.dto.ChatMessageRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅 메시지 전송 처리
    @MessageMapping("/chat/rooms/{roomId}/messages")
    public void create(
            @DestinationVariable Long roomId,
            @Valid @Payload ChatMessageRequestDto dto,
            Principal principal
    ) {
        ChatMessageResponseDto response = chatMessageService.create(roomId, getMemberId(principal), dto.message());
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + roomId + "/messages", response);
    }

    private Long getMemberId(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof CustomUser user) {
            return user.getMemberId();
        }

        throw new AuthenticationCredentialsNotFoundException("인증 정보가 없습니다.");
    }
}
