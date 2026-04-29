package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatRoomEnterRequestDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterResponseDto;
import com.ktcloud.daangn.chat.dto.ChatRoomListResponseDto;
import com.ktcloud.daangn.chat.dto.ChatRoomReadRequestDto;
import com.ktcloud.daangn.chat.dto.ChatRoomReadResponseDto;

import java.util.List;

public interface ChatRoomService {
    ChatRoomEnterResponseDto enterDirectRoom(ChatRoomEnterRequestDto dto);

    List<ChatRoomListResponseDto> findDirectRooms(String memberEmail);

    ChatRoomReadResponseDto readDirectRoom(Long roomId, ChatRoomReadRequestDto dto);
}
