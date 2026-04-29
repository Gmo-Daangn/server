package com.ktcloud.daangn.chat.controller;

import com.ktcloud.daangn.chat.dto.ChatMessageDeleteRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageEditRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.service.ChatMessageService;
import com.ktcloud.daangn.config.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅 메시지 목록 조회
    @GetMapping("/rooms/{roomId}/messages")
    public BaseResponse<List<ChatMessageResponseDto>> list(
            @PathVariable Long roomId,
            @RequestParam String memberEmail
    ) {
        return BaseResponse.success(chatMessageService.list(roomId, memberEmail));
    }

    // 채팅 메시지 수정
    @PostMapping("/messages/{messageId}/edit")
    public BaseResponse<ChatMessageResponseDto> edit(
            @PathVariable Long messageId,
            @RequestBody ChatMessageEditRequestDto dto
    ) {
        ChatMessageResponseDto response = chatMessageService.edit(messageId, dto);
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + response.roomId() + "/messages", response);

        return BaseResponse.success(response);
    }

    // 채팅 메시지 삭제
    @PostMapping("/messages/{messageId}/delete")
    public BaseResponse<ChatMessageResponseDto> delete(
            @PathVariable Long messageId,
            @RequestBody ChatMessageDeleteRequestDto dto
    ) {
        ChatMessageResponseDto response = chatMessageService.delete(messageId, dto);
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + response.roomId() + "/messages", response);

        return BaseResponse.success(response);
    }
}
