package com.sentimentum.api.message;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateAnalysisResultRequest(
        @NotNull UUID messageId,
        @NotNull Sentiment sentiment,
        @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal confidence
) {
}
