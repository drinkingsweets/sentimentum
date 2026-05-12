package com.sentimentum.api.message.stats;

import com.sentimentum.api.message.SentimentStatsDto;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SentimentStatsService {

    private final List<SentimentStatsStrategy> strategies;

    public SentimentStatsService(List<SentimentStatsStrategy> strategies) {
        this.strategies = strategies;
    }

    @Transactional(readOnly = true)
    public List<SentimentStatsDto> calculate(UUID projectId) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(projectId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No sentiment stats strategy for projectId: " + projectId))
                .calculate(projectId);
    }
}
