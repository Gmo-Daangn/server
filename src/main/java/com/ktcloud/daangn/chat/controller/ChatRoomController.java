package com.ktcloud.daangn.chat.controller;

import com.ktcloud.daangn.chat.dto.*;
import com.ktcloud.daangn.chat.service.ChatRoomService;
import com.ktcloud.daangn.config.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅방 생성 또는 입장 처리
    @PostMapping("/enter")
    public BaseResponse<ChatRoomEnterResponseDto> enterDirectRoom(@RequestBody ChatRoomEnterRequestDto dto) {
        return BaseResponse.success(chatRoomService.enterDirectRoom(dto));
    }

    // 내가 참여한 채팅방 목록 조회
    @GetMapping
    public BaseResponse<List<ChatRoomListResponseDto>> findDirectRooms(@RequestParam String memberEmail) {
        return BaseResponse.success(chatRoomService.findDirectRooms(memberEmail));
    }

    // 채팅방 읽음 처리
    @PostMapping("/{roomId}/read")
    public BaseResponse<ChatRoomReadResponseDto> readDirectRoom(
            @PathVariable Long roomId,
            @RequestBody ChatRoomReadRequestDto dto
    ) {
        ChatRoomReadResponseDto response = chatRoomService.readDirectRoom(roomId, dto);
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + roomId + "/read-status", response);

        return BaseResponse.success(response);
    }
}
