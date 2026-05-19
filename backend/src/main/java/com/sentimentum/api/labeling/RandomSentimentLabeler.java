package com.sentimentum.api.labeling;

import com.sentimentum.api.message.Sentiment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomSentimentLabeler {

    public SentimentLabel label() {
        Sentiment[] sentiments = Sentiment.values();
        Sentiment sentiment = sentiments[ThreadLocalRandom.current().nextInt(sentiments.length)];
        BigDecimal confidence = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0.50, 0.96))
                .setScale(2, RoundingMode.HALF_UP);
        return new SentimentLabel(sentiment, confidence);
    }
}
