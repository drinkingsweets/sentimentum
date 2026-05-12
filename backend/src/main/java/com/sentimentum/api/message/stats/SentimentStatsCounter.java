package com.sentimentum.api.message.stats;

import com.sentimentum.api.message.AnalysisResult;
import com.sentimentum.api.message.Sentiment;
import com.sentimentum.api.message.SentimentStatsDto;
import java.util.EnumMap;
import java.util.List;

final class SentimentStatsCounter {

    private SentimentStatsCounter() {
    }

    static List<SentimentStatsDto> count(List<AnalysisResult> results) {
        EnumMap<Sentiment, Long> counts = new EnumMap<>(Sentiment.class);
        for (Sentiment sentiment : Sentiment.values()) {
            counts.put(sentiment, 0L);
        }
        results.forEach(result -> counts.compute(result.getSentiment(), (key, value) -> value + 1));
        return counts.entrySet().stream()
                .map(entry -> new SentimentStatsDto(entry.getKey(), entry.getValue()))
                .toList();
    }
}
