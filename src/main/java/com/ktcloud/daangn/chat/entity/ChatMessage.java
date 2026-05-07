package com.ktcloud.daangn.chat.entity;

import com.ktcloud.daangn.member.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private long readCount;

    @Column(nullable = false)
    private boolean edited;

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public ChatMessage(ChatRoom chatRoom, Member member, String message, long readCount) {
        this.chatRoom = chatRoom;
        this.member = member;
        this.message = message;
        this.readCount = readCount;
        this.edited = false;
        this.deleted = false;
        this.createdAt = LocalDateTime.now();
    }

    public static ChatMessage createMessage(ChatRoom chatRoom, Member member, String message, long readCount) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(member)
                .message(message)
                .readCount(readCount)
                .build();
    }

    public void edit(String message) {
        this.message = message;
        this.edited = true;
    }

    public void delete() {
        this.message = "삭제된 메시지입니다.";
        this.deleted = true;
    }

    public void markRead() {
        this.readCount = 0;
    }

    public boolean isWrittenBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public boolean isDeletedMessage() {
        return this.deleted;
    }
}
