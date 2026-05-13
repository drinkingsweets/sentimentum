package com.sentimentum.api.youtube;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ImportYouTubeCommentsRequest(
        @NotNull UUID projectId,
        @NotBlank String video,
        @Min(1) @Max(100) Integer maxResults
) {
}
