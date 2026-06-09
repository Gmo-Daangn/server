package com.ktcloud.daangn.chat.repository;

import com.ktcloud.daangn.chat.dto.ChatRoomListResponseDto;
import com.ktcloud.daangn.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByMember_Id(Long memberId);

    List<ChatParticipant> findByChatRoom_Id(Long roomId);

    Optional<ChatParticipant> findByChatRoom_IdAndMember_Id(Long roomId, Long memberId);

    long countByChatRoom_Id(Long roomId);

    @Query("""
            select participant
            from ChatParticipant participant
            join fetch participant.member
            where participant.chatRoom.id = :roomId
            """)
    List<ChatParticipant> findByChatRoomIdWithMember(Long roomId);

    @Query("""
            select new com.ktcloud.daangn.chat.dto.ChatRoomListResponseDto(
                room.id,
                room.productId,
                otherParticipant.member.id,
                otherParticipant.member.nickName,
                room.lastMessage,
                room.lastMessageCreatedAt,
                myParticipant.unreadCount
            )
            from ChatParticipant myParticipant
            join myParticipant.chatRoom room
            join ChatParticipant otherParticipant
              on otherParticipant.chatRoom.id = room.id
             and otherParticipant.member.id <> :memberId
            where myParticipant.member.id = :memberId
            order by coalesce(room.lastMessageCreatedAt, room.createdAt) desc
            """)
    List<ChatRoomListResponseDto> findDirectRoomListByMemberId(Long memberId);
}
