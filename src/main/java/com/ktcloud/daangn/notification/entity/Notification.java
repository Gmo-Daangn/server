package com.ktcloud.daangn.notification.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name="notification")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long receiverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private NotificationTemplate template;

    private boolean isRead = false;
    private boolean isDeleted = false;
    private String message;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Notification(Long receiverId, NotificationTemplate template, String message) {
        this.receiverId = receiverId;
        this.template = template;
        this.message = message;
        this.isRead = false;
        this.isDeleted = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
