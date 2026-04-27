package com.ktcloud.daangn.chat.repository;

import com.ktcloud.daangn.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    public List<ChatParticipant> findByMemberId(Long memberId);
}
