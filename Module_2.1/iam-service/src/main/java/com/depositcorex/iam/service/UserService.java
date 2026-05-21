package com.depositcorex.iam.service;

import com.depositcorex.iam.dto.UserDTO;
import com.depositcorex.iam.entity.User;
import com.depositcorex.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with email: " + email));
        return toDTO(user);
    }

    public UserDTO getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return toDTO(user);
    }

    public List<UserDTO> getAll() {
        return userRepository.findAll().stream().map(this::toDTO).toList();
    }

    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setActive(true);
        userRepository.save(user);
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setPassword(null);
        dto.setActive(user.isActive());
        return dto;
    }
}
