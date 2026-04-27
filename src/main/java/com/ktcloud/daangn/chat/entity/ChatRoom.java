package com.ktcloud.daangn.chat.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "chat_rooms")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    @Enumerated(EnumType.STRING)
    private ChatType type;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public ChatRoom(Long productId, ChatType type) {
        this.productId = productId;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public static ChatRoom createRoom(Long productId, ChatType type) {
        return ChatRoom.builder()
                .productId(productId)
                .type(type)
                .build();
    }
}
