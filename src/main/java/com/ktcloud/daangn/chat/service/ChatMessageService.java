package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatMessageDeleteRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageEditRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;

import java.util.List;

public interface ChatMessageService {
    ChatMessageResponseDto create(Long roomId, ChatMessageRequestDto dto);

    List<ChatMessageResponseDto> list(Long roomId, String memberEmail);

    ChatMessageResponseDto edit(Long messageId, ChatMessageEditRequestDto dto);

    ChatMessageResponseDto delete(Long messageId, ChatMessageDeleteRequestDto dto);
}
