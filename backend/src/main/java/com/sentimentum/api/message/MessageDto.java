package com.sentimentum.api.message;

import java.time.Instant;
import java.util.UUID;

public record MessageDto(
        UUID id,
        String content,
        String author,
        UUID sourceId,
        String language,
        String tag,
        Instant processedAt,
        Instant createdAt
) {

    static MessageDto from(Message message) {
        return new MessageDto(
                message.getId(),
                message.getContent(),
                message.getAuthor(),
                message.getSource().getId(),
                message.getLanguage(),
                message.getTag(),
                message.getProcessedAt(),
                message.getCreatedAt()
        );
    }
}
