package com.sentimentum.api.project;

import java.time.Instant;
import java.util.UUID;

public record ProjectDto(
        UUID id,
        String name,
        String description,
        UUID ownerId,
        Instant createdAt,
        Instant updatedAt
) {

    static ProjectDto from(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getId(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
