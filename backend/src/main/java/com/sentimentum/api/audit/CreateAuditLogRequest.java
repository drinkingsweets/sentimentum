package com.sentimentum.api.audit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAuditLogRequest(@NotNull UUID userId, @NotBlank String action) {
}
