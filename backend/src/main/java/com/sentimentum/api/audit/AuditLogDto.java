package com.sentimentum.api.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditLogDto(UUID id, UUID userId, String action, Instant createdAt) {

    static AuditLogDto from(AuditLog log) {
        return new AuditLogDto(log.getId(), log.getUser().getId(), log.getAction(), log.getCreatedAt());
    }
}
