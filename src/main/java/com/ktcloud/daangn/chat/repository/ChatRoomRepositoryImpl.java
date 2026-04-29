package com.ktcloud.daangn.chat.repository;

import com.ktcloud.daangn.chat.entity.ChatRoom;
import com.ktcloud.daangn.chat.entity.ChatType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepository {

    private final EntityManager entityManager;

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        if (chatRoom.getId() == null) {
            entityManager.persist(chatRoom);
            return chatRoom;
        }

        return entityManager.merge(chatRoom);
    }

    @Override
    public Optional<ChatRoom> findById(Long roomId) {
        return Optional.ofNullable(entityManager.find(ChatRoom.class, roomId));
    }

    @Override
    public List<ChatRoom> findExistingDirectRoom(String memberEmail, String targetMemberEmail, Long productId, ChatType type) {
        return entityManager.createQuery("""
                        select room
                        from ChatRoom room
                        where room.type = :type
                          and 2 = (
                            select count(participantCount)
                            from ChatParticipant participantCount
                            where participantCount.chatRoom.id = room.id
                          )
                          and room.id in (
                            select participant.chatRoom.id
                            from ChatParticipant participant
                            where participant.member.email = :memberEmail
                          )
                          and room.id in (
                            select participant.chatRoom.id
                            from ChatParticipant participant
                            where participant.member.email = :targetMemberEmail
                          )
                          and (
                            (:productId is null and room.productId is null)
                            or room.productId = :productId
                          )
                        order by room.id asc
                        """,
                        ChatRoom.class
                )
                .setParameter("memberEmail", memberEmail)
                .setParameter("targetMemberEmail", targetMemberEmail)
                .setParameter("productId", productId)
                .setParameter("type", type)
                .getResultList();
    }
}
