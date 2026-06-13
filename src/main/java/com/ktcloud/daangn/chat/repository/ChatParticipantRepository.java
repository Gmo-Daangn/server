package com.ktcloud.daangn.chat.repository;

import com.ktcloud.daangn.chat.dto.ChatRoomListResponseDto;
import com.ktcloud.daangn.chat.entity.ChatParticipant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByMember_Id(Long memberId);

    List<ChatParticipant> findByChatRoom_Id(Long roomId);

    // 단순 참여 여부 확인용 조회. 구독 검증처럼 상태 변경이 없는 경로에서는 락을 잡지 않는다.
    Optional<ChatParticipant> findByChatRoom_IdAndMember_Id(Long roomId, Long memberId);

    long countByChatRoom_Id(Long roomId);

    // 읽음 처리처럼 조회 직후 unreadCount/lastReadMessageId를 갱신하는 경로에서만 쓰는 락 조회.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select participant
            from ChatParticipant participant
            where participant.chatRoom.id = :roomId
              and participant.member.id = :memberId
            """)
    Optional<ChatParticipant> findByChatRoomIdAndMemberIdForUpdate(
            @Param("roomId") Long roomId,
            @Param("memberId") Long memberId
    );

    @Query("""
            select participant
            from ChatParticipant participant
            join fetch participant.member
            where participant.chatRoom.id = :roomId
            """)
    List<ChatParticipant> findByChatRoomIdWithMember(Long roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select participant
            from ChatParticipant participant
            join fetch participant.member
            where participant.chatRoom.id = :roomId
            """)
    List<ChatParticipant> findByChatRoomIdWithMemberForUpdate(@Param("roomId") Long roomId);

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
              and room.type = com.ktcloud.daangn.chat.entity.ChatType.PRODUCT
            order by coalesce(room.lastMessageCreatedAt, room.createdAt) desc
            """)
    List<ChatRoomListResponseDto> findDirectRoomListByMemberId(Long memberId);
}
