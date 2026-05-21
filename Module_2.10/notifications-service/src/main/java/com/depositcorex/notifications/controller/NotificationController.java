package com.depositcorex.notifications.controller;

import com.depositcorex.notifications.dto.NotificationRequest;
import com.depositcorex.notifications.entity.Notification;
import com.depositcorex.notifications.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<Notification> send(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.send(request));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<List<Notification>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getByUser(userId));
    }

    @GetMapping("/user/{userId}/status/{status}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<List<Notification>> getByUserAndStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        return ResponseEntity.ok(notificationService.getByUserAndStatus(userId, status));
    }

    @GetMapping("/{notificationId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<Notification> getById(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.getById(notificationId));
    }

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<List<Notification>> getPending() {
        return ResponseEntity.ok(notificationService.getPending());
    }
}
