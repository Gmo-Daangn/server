package com.ktcloud.daangn.chat.controller;

import com.ktcloud.daangn.chat.dto.ChatRoomEnterRequestDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterResponseDto;
import com.ktcloud.daangn.chat.service.ChatServiceImpl;
import com.ktcloud.daangn.config.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatServiceImpl chatService;

    @PostMapping(path = "/rooms/enter")
    public BaseResponse<ChatRoomEnterResponseDto> enterRoom(@RequestBody ChatRoomEnterRequestDto dto) {
        // 기존 채팅 룸 없으면 생성

        // 일단 새로운 방을 생성하는 로직으로 테스트
        return BaseResponse.success(chatService.CreateRoom(dto));
    }

}
