package com.depositcorex.iam.dto;

import com.depositcorex.iam.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    @NotBlank private String name;
    private UserRole role;
    @Email @NotBlank private String email;
    private String phone;
    @NotBlank private String password;
    private boolean active;
}
