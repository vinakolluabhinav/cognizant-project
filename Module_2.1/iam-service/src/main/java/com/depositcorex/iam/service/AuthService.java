package com.depositcorex.iam.service;

import com.depositcorex.iam.dto.LoginRequest;
import com.depositcorex.iam.dto.LoginResponse;
import com.depositcorex.iam.dto.UserDTO;
import com.depositcorex.iam.entity.User;
import com.depositcorex.iam.exception.AccountDeactivatedException;
import com.depositcorex.iam.exception.BadCredentialsException;
import com.depositcorex.iam.repository.UserRepository;
import com.depositcorex.iam.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!user.isActive()) {
            throw new AccountDeactivatedException("Your account has been deactivated. Please contact the administrator.");
        }

        auditService.log(user.getUserId(), "LOGIN", "/api/v1/auth/login", null);
        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, "Bearer", user.getUserId(), user.getName(), user.getRole().name());
    }

    public UserDTO registerUser(UserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Email already registered: " + dto.getEmail());
        }
        User user = User.builder()
                .name(dto.getName())
                .role(dto.getRole())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .password(passwordEncoder.encode(dto.getPassword()))
                .active(true)
                .build();
        user = userRepository.save(user);
        dto.setUserId(user.getUserId());
        dto.setPassword(null);
        return dto;
    }
}
