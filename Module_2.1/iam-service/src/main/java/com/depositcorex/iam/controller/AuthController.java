package com.depositcorex.iam.controller;

import com.depositcorex.iam.dto.LoginRequest;
import com.depositcorex.iam.dto.LoginResponse;
import com.depositcorex.iam.dto.UserDTO;
import com.depositcorex.iam.entity.UserRole;
import com.depositcorex.iam.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('CORE_ADMIN')")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(dto));
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody UserDTO dto) {
        dto.setRole(UserRole.CUSTOMER);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(dto));
    }
}
