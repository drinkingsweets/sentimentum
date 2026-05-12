package com.sentimentum.api.message;

import com.sentimentum.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    public MessageController(MessageService service) {
        this.service = service;
    }

    @GetMapping("/messages")
    public List<MessageDto> listMessages(@AuthenticationPrincipal AuthenticatedUser user, @RequestParam(required = false) UUID sourceId) {
        return service.list(sourceId, user.getId());
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto createMessage(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateMessageRequest request) {
        return service.create(request, user.getId());
    }

    @GetMapping("/analysis-results")
    public List<AnalysisResultDto> listResults(@AuthenticationPrincipal AuthenticatedUser user, @RequestParam(required = false) UUID messageId) {
        return service.listResults(messageId, user.getId());
    }

    @PostMapping("/analysis-results")
    @ResponseStatus(HttpStatus.CREATED)
    public AnalysisResultDto createResult(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateAnalysisResultRequest request) {
        return service.addResult(request, user.getId());
    }

    @GetMapping("/analytics/sentiment-stats")
    public List<SentimentStatsDto> sentimentStats(@AuthenticationPrincipal AuthenticatedUser user, @RequestParam(required = false) UUID projectId) {
        return service.sentimentStats(projectId, user.getId());
    }
}
