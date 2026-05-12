package com.sentimentum.api.message.stats;

import com.sentimentum.api.message.AnalysisResultRepository;
import com.sentimentum.api.message.SentimentStatsDto;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProjectSentimentStatsStrategy implements SentimentStatsStrategy {

    private final AnalysisResultRepository results;

    public ProjectSentimentStatsStrategy(AnalysisResultRepository results) {
        this.results = results;
    }

    @Override
    public boolean supports(UUID projectId) {
        return projectId != null;
    }

    @Override
    public List<SentimentStatsDto> calculate(UUID projectId) {
        return SentimentStatsCounter.count(results.findByMessageSourceProjectId(projectId));
    }
}
