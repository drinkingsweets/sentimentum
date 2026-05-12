package com.sentimentum.api.message.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentimentum.api.message.Sentiment;
import com.sentimentum.api.message.SentimentStatsDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SentimentStatsServiceTest {

    @Test
    void usesAllMessagesStrategyWhenProjectIdIsMissing() {
        SentimentStatsService service = new SentimentStatsService(List.of(
                new FixedStrategy(true, List.of(new SentimentStatsDto(Sentiment.POSITIVE, 3))),
                new FixedStrategy(false, List.of(new SentimentStatsDto(Sentiment.NEGATIVE, 5)))
        ));

        List<SentimentStatsDto> stats = service.calculate(null);

        assertEquals(List.of(new SentimentStatsDto(Sentiment.POSITIVE, 3)), stats);
    }

    @Test
    void usesProjectStrategyWhenProjectIdIsPresent() {
        UUID projectId = UUID.randomUUID();
        SentimentStatsService service = new SentimentStatsService(List.of(
                new FixedStrategy(false, List.of(new SentimentStatsDto(Sentiment.POSITIVE, 3))),
                new FixedStrategy(true, List.of(new SentimentStatsDto(Sentiment.NEGATIVE, 5)))
        ));

        List<SentimentStatsDto> stats = service.calculate(projectId);

        assertEquals(List.of(new SentimentStatsDto(Sentiment.NEGATIVE, 5)), stats);
    }

    private record FixedStrategy(boolean supports, List<SentimentStatsDto> stats) implements SentimentStatsStrategy {

        @Override
        public boolean supports(UUID projectId) {
            return supports;
        }

        @Override
        public List<SentimentStatsDto> calculate(UUID projectId) {
            return stats;
        }
    }
}
