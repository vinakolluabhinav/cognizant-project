package com.depositcorex.notifications.service;

import com.depositcorex.notifications.client.IamClient;
import com.depositcorex.notifications.dto.NotificationRequest;
import com.depositcorex.notifications.entity.Notification;
import com.depositcorex.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final IamClient iamClient;

    public Notification send(NotificationRequest request) {
        // User existence is already validated by the API Gateway JWT filter
        // No need to call IAM service again
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .message(request.getMessage())
                .category(request.getCategory())
                .channel(request.getChannel() != null ? request.getChannel() : "IN_APP")
                .status("SENT")
                .sentDate(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    public List<Notification> getByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    public List<Notification> getByUserAndStatus(Long userId, String status) {
        return notificationRepository.findByUserIdAndStatusOrderByCreatedDateDesc(userId, status);
    }

    public Notification getById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification = getById(notificationId);
        notification.setStatus("READ");
        return notificationRepository.save(notification);
    }

    public List<Notification> getPending() {
        return notificationRepository.findByStatus("PENDING");
    }
}
