package com.depositcorex.notifications.repository;

import com.depositcorex.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedDateDesc(Long userId);
    List<Notification> findByUserIdAndStatusOrderByCreatedDateDesc(Long userId, String status);
    List<Notification> findByStatus(String status);
}
