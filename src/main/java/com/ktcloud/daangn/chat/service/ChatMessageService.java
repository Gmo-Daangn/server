package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;

import java.util.List;

public interface ChatMessageService {
    ChatMessageResponseDto create(Long roomId, Long memberId, String message);

    List<ChatMessageResponseDto> list(Long roomId, Long memberId);

    ChatMessageResponseDto edit(Long messageId, Long memberId, String message);

    ChatMessageResponseDto delete(Long messageId, Long memberId);
}
