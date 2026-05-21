package com.cts.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    private User user;

    @Lob
    @Column(name = "Message")
    private String message;

    @Column(name = "Category", length = 60)
    private String category;

    @Column(name = "Status", length = 30)
    private String status;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;
}