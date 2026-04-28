package com.ktcloud.daangn.notification.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="notification_template")
@Getter
@NoArgsConstructor
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String templateType;
    private String templateTitle;
    private String templateText;
    private Long identifier;

    @Builder
    public NotificationTemplate(String templateType, String templateTitle, String templateText, Long identifier) {
        this.templateType = templateType;
        this.templateTitle = templateTitle;
        this.templateText = templateText;
        this.identifier = identifier;
    }
}
