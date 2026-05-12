package com.sentimentum.api.audit;

import jakarta.validation.constraints.NotBlank;

public record CreateAuditLogRequest(@NotBlank String action) {
}
