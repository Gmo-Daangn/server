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
    @JoinColumn(name = "room_id")
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String message;

    private Long readCount;

    private boolean isEdit;

    private LocalDateTime createdAt;

    @Builder
    public ChatMessage(ChatRoom chatRoom, Member member, String message, Long readCount) {
        this.chatRoom = chatRoom;
        this.member = member;
        this.message = message;
        this.readCount = readCount;
        this.isEdit = false;
        this.createdAt = LocalDateTime.now();
    }

    public static ChatMessage createMessage(ChatRoom chatRoom, Member member, String message, Long readCount) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(member)
                .message(message)
                .readCount(readCount)
                .build();
    }
}
