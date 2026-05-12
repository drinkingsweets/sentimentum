package com.sentimentum.api.datasource;

import java.time.Instant;
import java.util.UUID;

public record DataSourceDto(
        UUID id,
        String name,
        String link,
        DataSourceType type,
        UUID projectId,
        Instant createdAt
) {

    static DataSourceDto from(DataSource source) {
        return new DataSourceDto(
                source.getId(),
                source.getName(),
                source.getLink(),
                source.getType(),
                source.getProject().getId(),
                source.getCreatedAt()
        );
    }
}
