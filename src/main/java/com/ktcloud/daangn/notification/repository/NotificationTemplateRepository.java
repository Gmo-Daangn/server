package com.ktcloud.daangn.notification.repository;

import com.ktcloud.daangn.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByTemplateType(String templateType);
}
