package com.sentimentum.api.audit;

import com.sentimentum.api.user.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogs;
    private final UserService users;

    public AuditLogService(AuditLogRepository auditLogs, UserService users) {
        this.auditLogs = auditLogs;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> list(UUID userId) {
        List<AuditLog> result = auditLogs.findByUserId(userId);
        return result.stream().map(AuditLogDto::from).toList();
    }

    @Transactional
    public AuditLogDto create(CreateAuditLogRequest request, UUID userId) {
        return AuditLogDto.from(auditLogs.save(new AuditLog(users.getEntity(userId), request.action())));
    }
}
