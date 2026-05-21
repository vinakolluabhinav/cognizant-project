package com.depositcorex.iam.service;

import com.depositcorex.iam.entity.AuditLog;
import com.depositcorex.iam.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(Long userId, String action, String resource, String metadata) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action(action)
                .resource(resource)
                .metadata(metadata)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsForUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}
