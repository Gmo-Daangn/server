package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;

import java.util.List;

public interface ChatMessageService {
    ChatMessageResponseDto create(Long roomId, String memberEmail, String message);

    List<ChatMessageResponseDto> list(Long roomId, String memberEmail);

    ChatMessageResponseDto edit(Long messageId, String memberEmail, String message);

    ChatMessageResponseDto delete(Long messageId, String memberEmail);
}
