package com.depositcorex.Main.Client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notifications-service")
public interface NotificationClient {

    @PostMapping("/api/v1/notifications/send")
    Object sendNotification(@RequestBody NotificationRequest request);

    @Data
    class NotificationRequest {
        private Long userId;
        private String message;
        private String category;
        private String channel;

        public NotificationRequest(Long userId, String message, String category) {
            this.userId   = userId;
            this.message  = message;
            this.category = category;
            this.channel  = "IN_APP";
        }
    }
}
