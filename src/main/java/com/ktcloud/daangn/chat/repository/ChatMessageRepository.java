package com.ktcloud.daangn.chat.repository;

import com.ktcloud.daangn.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    public List<ChatMessage> findByMemberEmail(String memberEmail);
}
