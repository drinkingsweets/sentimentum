package com.sentimentum.api.message;

import com.sentimentum.api.message.stats.SentimentStatsService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageService service;
    private final SentimentStatsService statsService;

    public MessageController(MessageService service, SentimentStatsService statsService) {
        this.service = service;
        this.statsService = statsService;
    }

    @GetMapping("/messages")
    public List<MessageDto> listMessages(@RequestParam(required = false) UUID sourceId) {
        return service.list(sourceId);
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto createMessage(@Valid @RequestBody CreateMessageRequest request) {
        return service.create(request);
    }

    @GetMapping("/analysis-results")
    public List<AnalysisResultDto> listResults(@RequestParam(required = false) UUID messageId) {
        return service.listResults(messageId);
    }

    @PostMapping("/analysis-results")
    @ResponseStatus(HttpStatus.CREATED)
    public AnalysisResultDto createResult(@Valid @RequestBody CreateAnalysisResultRequest request) {
        return service.addResult(request);
    }

    @GetMapping("/analytics/sentiment-stats")
    public List<SentimentStatsDto> sentimentStats(@RequestParam(required = false) UUID projectId) {
        return statsService.calculate(projectId);
    }
}
