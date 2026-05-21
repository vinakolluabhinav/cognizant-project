package com.depositcorex.notifications.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "iam-service")
public interface IamClient {

    @GetMapping("/api/v1/users/{userId}")
    UserDTO getUserById(@PathVariable Long userId);

    @Data
    class UserDTO {
        private Long userId;
        private String name;
        private String email;
        private String phone;
        private String role;
        private boolean active;
    }
}
