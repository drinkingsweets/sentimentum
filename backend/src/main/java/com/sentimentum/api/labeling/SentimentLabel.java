package com.sentimentum.api.labeling;

import com.sentimentum.api.message.Sentiment;
import java.math.BigDecimal;

public record SentimentLabel(Sentiment sentiment, BigDecimal confidence) {
}
