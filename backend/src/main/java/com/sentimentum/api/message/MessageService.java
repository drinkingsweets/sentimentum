package com.sentimentum.api.message;

import com.sentimentum.api.common.NotFoundException;
import com.sentimentum.api.datasource.DataSourceService;
import java.time.Instant;
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
    public List<MessageDto> list(UUID sourceId) {
        List<Message> result = sourceId == null ? messages.findAll() : messages.findBySourceId(sourceId);
        return result.stream().map(MessageDto::from).toList();
    }

    @Transactional
    public MessageDto create(CreateMessageRequest request) {
        Message message = new Message(
                request.content(),
                request.author(),
                sources.getEntity(request.sourceId()),
                request.language(),
                request.tag(),
                request.createdAt()
        );
        return MessageDto.from(messages.save(message));
    }

    @Transactional
    public AnalysisResultDto addResult(CreateAnalysisResultRequest request) {
        Message message = getEntity(request.messageId());
        message.setProcessedAt(Instant.now());
        AnalysisResult result = new AnalysisResult(message, request.sentiment(), request.confidence());
        return AnalysisResultDto.from(results.save(result));
    }

    @Transactional(readOnly = true)
    public List<AnalysisResultDto> listResults(UUID messageId) {
        List<AnalysisResult> result = messageId == null ? results.findAll() : results.findByMessageId(messageId);
        return result.stream().map(AnalysisResultDto::from).toList();
    }

    @Transactional(readOnly = true)
    public Message getEntity(UUID id) {
        return messages.findById(id).orElseThrow(() -> new NotFoundException("Message not found: " + id));
    }
}
