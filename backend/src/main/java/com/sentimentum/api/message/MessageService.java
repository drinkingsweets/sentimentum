package com.sentimentum.api.message;

import com.sentimentum.api.common.NotFoundException;
import com.sentimentum.api.datasource.DataSourceService;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository messages;
    private final AnalysisResultRepository results;
    private final DataSourceService sources;

    public MessageService(MessageRepository messages, AnalysisResultRepository results, DataSourceService sources) {
        this.messages = messages;
        this.results = results;
        this.sources = sources;
    }

    @Transactional(readOnly = true)
    public List<MessageDto> list(UUID sourceId, UUID ownerId) {
        List<Message> result = sourceId == null
                ? messages.findBySourceProjectOwnerId(ownerId)
                : messages.findBySourceIdAndSourceProjectOwnerId(sourceId, ownerId);
        return result.stream().map(MessageDto::from).toList();
    }

    @Transactional
    public MessageDto create(CreateMessageRequest request, UUID ownerId) {
        Message message = new Message(
                request.content(),
                request.author(),
                sources.getOwnedEntity(request.sourceId(), ownerId),
                request.language(),
                request.tag(),
                request.createdAt()
        );
        return MessageDto.from(messages.save(message));
    }

    @Transactional
    public AnalysisResultDto addResult(CreateAnalysisResultRequest request, UUID ownerId) {
        Message message = getOwnedEntity(request.messageId(), ownerId);
        message.setProcessedAt(Instant.now());
        AnalysisResult result = new AnalysisResult(message, request.sentiment(), request.confidence());
        return AnalysisResultDto.from(results.save(result));
    }

    @Transactional(readOnly = true)
    public List<AnalysisResultDto> listResults(UUID messageId, UUID ownerId) {
        List<AnalysisResult> result = messageId == null
                ? results.findByMessageSourceProjectOwnerId(ownerId)
                : results.findByMessageIdAndMessageSourceProjectOwnerId(messageId, ownerId);
        return result.stream().map(AnalysisResultDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SentimentStatsDto> sentimentStats(UUID projectId, UUID ownerId) {
        EnumMap<Sentiment, Long> counts = new EnumMap<>(Sentiment.class);
        for (Sentiment sentiment : Sentiment.values()) {
            counts.put(sentiment, 0L);
        }
        List<AnalysisResult> source = projectId == null
                ? results.findByMessageSourceProjectOwnerId(ownerId)
                : results.findByMessageSourceProjectIdAndMessageSourceProjectOwnerId(projectId, ownerId);
        source.forEach(result -> counts.compute(result.getSentiment(), (key, value) -> value + 1));
        return counts.entrySet().stream()
                .map(entry -> new SentimentStatsDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Message getEntity(UUID id) {
        return messages.findById(id).orElseThrow(() -> new NotFoundException("Message not found: " + id));
    }

    @Transactional(readOnly = true)
    public Message getOwnedEntity(UUID id, UUID ownerId) {
        return messages.findByIdAndSourceProjectOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("Message not found: " + id));
    }
}
