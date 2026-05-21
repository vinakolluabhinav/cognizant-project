package com.depositcorex.iam.controller;

import com.depositcorex.iam.dto.UserDTO;
import com.depositcorex.iam.entity.AuditLog;
import com.depositcorex.iam.service.AuditService;
import com.depositcorex.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('CORE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/by-email")
    @PreAuthorize("hasAnyRole('CORE_ADMIN', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER')")
    public ResponseEntity<UserDTO> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CORE_ADMIN') or authentication.principal == #id.toString()")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('CORE_ADMIN')")
    public ResponseEntity<String> deactivate(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok("User deactivated");
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('CORE_ADMIN')")
    public ResponseEntity<String> activate(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok("User activated");
    }

    @GetMapping("/{id}/audit-logs")
    @PreAuthorize("hasRole('CORE_ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getLogsForUser(id));
    }
}
