package com.sentimentum.api.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateMessageRequest(
        @NotBlank String content,
        String author,
        @NotNull UUID sourceId,
        @NotBlank String language,
        @NotBlank String tag,
        Instant createdAt
) {
}
