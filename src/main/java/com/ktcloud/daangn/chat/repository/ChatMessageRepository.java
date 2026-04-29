package com.ktcloud.daangn.chat.repository;

import com.ktcloud.daangn.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoom_IdOrderByIdAsc(Long roomId);

    Optional<ChatMessage> findTopByChatRoom_IdOrderByIdDesc(Long roomId);

    long countByChatRoom_IdAndMember_EmailNotAndReadCountGreaterThan(Long roomId, String memberEmail, long readCount);
}
