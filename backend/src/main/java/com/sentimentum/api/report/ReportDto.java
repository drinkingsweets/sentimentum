package com.sentimentum.api.report;

import java.time.Instant;
import java.util.UUID;

public record ReportDto(
        UUID id,
        UUID userId,
        UUID projectId,
        String title,
        String data,
        ReportFormat format,
        Instant createdAt
) {

    static ReportDto from(Report report) {
        return new ReportDto(
                report.getId(),
                report.getUser().getId(),
                report.getProject().getId(),
                report.getTitle(),
                report.getData(),
                report.getFormat(),
                report.getCreatedAt()
        );
    }
}
