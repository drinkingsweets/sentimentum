package com.sentimentum.api.datasource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateDataSourceRequest(
        @NotBlank String name,
        @NotBlank String link,
        @NotNull DataSourceType type,
        @NotNull UUID projectId
) {
}
