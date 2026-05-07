package com.ktcloud.daangn.chat.controller;

import com.ktcloud.daangn.chat.dto.ChatMessageDeleteRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.dto.ChatMessageWriteRequestDto;
import com.ktcloud.daangn.chat.service.ChatMessageService;
import com.ktcloud.daangn.config.dto.BaseResponse;
import jakarta.validation.Valid;
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
    @GetMapping("/messages/{roomId}")
    public BaseResponse<List<ChatMessageResponseDto>> list(
            @PathVariable Long roomId,
            @RequestParam Long memberId
    ) {
        return BaseResponse.success(chatMessageService.list(roomId, memberId));
    }

    // 채팅 메시지 수정
    @PatchMapping("/messages/{messageId}")
    public BaseResponse<ChatMessageResponseDto> edit(
            @PathVariable Long messageId,
            @Valid @RequestBody ChatMessageWriteRequestDto dto
    ) {
        ChatMessageResponseDto response = chatMessageService.edit(messageId, dto.memberId(), dto.message());
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + response.roomId() + "/messages", response);

        return BaseResponse.success(response);
    }

    // 채팅 메시지 삭제
    @DeleteMapping("/messages/{messageId}")
    public BaseResponse<ChatMessageResponseDto> delete(
            @PathVariable Long messageId,
            @Valid @RequestBody ChatMessageDeleteRequestDto dto
    ) {
        ChatMessageResponseDto response = chatMessageService.delete(messageId, dto.memberId());
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + response.roomId() + "/messages", response);

        return BaseResponse.success(response);
    }
}
