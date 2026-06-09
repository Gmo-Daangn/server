package com.ktcloud.daangn.chat.repository;

import com.ktcloud.daangn.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            select message
            from ChatMessage message
            join fetch message.chatRoom
            join fetch message.member
            where message.chatRoom.id = :roomId
            order by message.id asc
            """)
    List<ChatMessage> findByChatRoomIdWithRoomAndMemberOrderByIdAsc(Long roomId);

    @Query("""
            select message
            from ChatMessage message
            join fetch message.chatRoom
            join fetch message.member
            where message.chatRoom.id = :roomId
              and message.deleted = false
              and lower(message.message) like lower(concat('%', :keyword, '%')) escape '!'
              and (:beforeMessageId is null or message.id < :beforeMessageId)
            order by message.id desc
            """)
    List<ChatMessage> findMessagesByKeyword(
            @Param("roomId") Long roomId,
            @Param("keyword") String keyword,
            @Param("beforeMessageId") Long beforeMessageId,
            Pageable pageable
    );

    @Query("""
            select message
            from ChatMessage message
            join fetch message.chatRoom
            join fetch message.member
            where message.id = :messageId
            """)
    Optional<ChatMessage> findByIdWithRoomAndMember(Long messageId);

    @Query("""
            select max(message.id)
            from ChatMessage message
            where message.chatRoom.id = :roomId
            """)
    Optional<Long> findLatestMessageIdByRoomId(Long roomId);
}
