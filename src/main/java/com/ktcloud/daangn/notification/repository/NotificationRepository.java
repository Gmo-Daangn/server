package com.ktcloud.daangn.notification.repository;

import com.ktcloud.daangn.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("select n from Notification n where n.receiverId = :receiverId and n.isDeleted = false order by n.createdAt desc")
    List<Notification> findActiveByReceiverId(@Param("receiverId") Long receiverId);
}
