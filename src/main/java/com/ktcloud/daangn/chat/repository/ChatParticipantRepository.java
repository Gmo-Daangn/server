package com.ktcloud.daangn.chat.repository;

import com.ktcloud.daangn.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByMember_Id(Long memberId);

    List<ChatParticipant> findByChatRoom_Id(Long roomId);

    Optional<ChatParticipant> findByChatRoom_IdAndMember_Id(Long roomId, Long memberId);

    long countByChatRoom_Id(Long roomId);
}
