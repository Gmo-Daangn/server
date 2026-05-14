package com.ktcloud.daangn.chat.repository;

import com.ktcloud.daangn.chat.entity.ChatRoom;
import com.ktcloud.daangn.chat.entity.ChatType;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository {

    ChatRoom save(ChatRoom chatRoom);

    Optional<ChatRoom> findById(Long roomId);

    List<ChatRoom> findExistingDirectRoom(Long memberId, Long targetMemberId, Long productId, ChatType type);
}
