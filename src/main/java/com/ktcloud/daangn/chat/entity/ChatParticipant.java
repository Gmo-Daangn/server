package com.ktcloud.daangn.chat.entity;

import com.ktcloud.daangn.member.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_participants", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"room_id", "member_id"})
})
public class ChatParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private long unreadCount;

    private Long lastReadMessageId;

    @Builder
    public ChatParticipant(ChatRoom chatRoom, Member member) {
        this.chatRoom = chatRoom;
        this.member = member;
        this.unreadCount = 0;
    }

    public static ChatParticipant createParticipant(ChatRoom chatRoom, Member member) {
        return ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
    }

    public boolean isMember(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public boolean isNotMember(Long memberId) {
        return !isMember(memberId);
    }

    public void increaseUnreadCount() {
        this.unreadCount++;
    }

    public void markRead(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
        this.unreadCount = 0;
    }
}
