package com.sentimentum.api.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateReportRequest(
        @NotNull UUID userId,
        @NotNull UUID projectId,
        @NotBlank String title,
        @NotBlank String data,
        @NotNull ReportFormat format
) {
}
