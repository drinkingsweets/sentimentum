package com.sentimentum.api.message.stats;

import com.sentimentum.api.message.SentimentStatsDto;
import java.util.List;
import java.util.UUID;

public interface SentimentStatsStrategy {

    boolean supports(UUID projectId);

    List<SentimentStatsDto> calculate(UUID projectId);
}
