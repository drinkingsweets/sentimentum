package com.sentimentum.api.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateProjectRequest(
        @NotBlank String name,
        String description,
        @NotNull UUID ownerId
) {
}
