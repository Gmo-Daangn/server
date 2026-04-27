package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatMessageRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterRequestDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterResponseDto;
import com.ktcloud.daangn.chat.entity.ChatRoom;

import java.util.List;

public interface ChatService {

    ChatRoomEnterResponseDto CreateRoom(ChatRoomEnterRequestDto dto);

    ChatRoom EnterRoom(ChatRoomEnterRequestDto dto);

    ChatMessageResponseDto SendChat(ChatMessageRequestDto dto);

    List<ChatRoom> GetRoomList(String memberEmail);
}
