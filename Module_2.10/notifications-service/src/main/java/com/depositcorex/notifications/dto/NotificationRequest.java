package com.depositcorex.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String message;

    @NotBlank
    private String category;

    private String channel;
}
