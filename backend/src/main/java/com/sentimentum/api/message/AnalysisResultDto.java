package com.sentimentum.api.message;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AnalysisResultDto(
        UUID id,
        UUID messageId,
        Sentiment sentiment,
        BigDecimal confidence,
        Instant createdAt
) {

    static AnalysisResultDto from(AnalysisResult result) {
        return new AnalysisResultDto(
                result.getId(),
                result.getMessage().getId(),
                result.getSentiment(),
                result.getConfidence(),
                result.getCreatedAt()
        );
    }
}
