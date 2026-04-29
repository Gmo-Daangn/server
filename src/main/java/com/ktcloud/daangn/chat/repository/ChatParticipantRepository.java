package com.ktcloud.daangn.chat.repository;

import com.ktcloud.daangn.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByMember_Email(String memberEmail);

    List<ChatParticipant> findByChatRoom_Id(Long roomId);

    Optional<ChatParticipant> findByChatRoom_IdAndMember_Email(Long roomId, String memberEmail);

    long countByChatRoom_Id(Long roomId);
}
