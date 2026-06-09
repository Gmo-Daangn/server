package com.ktcloud.daangn.chat.controller;

import com.ktcloud.daangn.auth.dto.CustomUser;
import com.ktcloud.daangn.chat.dto.ChatMessageRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.service.ChatMessageService;
import com.ktcloud.daangn.common.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal CustomUser user
    ) {
        return BaseResponse.success(chatMessageService.list(roomId, user.getMemberId()));
    }

    // 채팅방 메시지 검색
    @GetMapping("/messages/{roomId}/search")
    public BaseResponse<List<ChatMessageResponseDto>> search(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUser user,
            @RequestParam String keyword,
            @RequestParam(required = false) Long beforeMessageId,
            @RequestParam(defaultValue = "30") int size
    ) {
        return BaseResponse.success(chatMessageService.search(roomId, user.getMemberId(), keyword, beforeMessageId, size));
    }

    // 채팅 메시지 수정
    @PatchMapping("/messages/{messageId}")
    public BaseResponse<ChatMessageResponseDto> edit(
            @PathVariable Long messageId,
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestBody ChatMessageRequestDto dto
    ) {
        ChatMessageResponseDto response = chatMessageService.edit(messageId, user.getMemberId(), dto.message());
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + response.roomId() + "/messages", response);

        return BaseResponse.success(response);
    }

    // 채팅 메시지 삭제
    @DeleteMapping("/messages/{messageId}")
    public BaseResponse<ChatMessageResponseDto> delete(
            @PathVariable Long messageId,
            @AuthenticationPrincipal CustomUser user
    ) {
        ChatMessageResponseDto response = chatMessageService.delete(messageId, user.getMemberId());
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + response.roomId() + "/messages", response);

        return BaseResponse.success(response);
    }
}
