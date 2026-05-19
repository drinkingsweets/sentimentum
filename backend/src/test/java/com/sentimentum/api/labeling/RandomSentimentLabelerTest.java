package com.sentimentum.api.labeling;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RandomSentimentLabelerTest {

    @Test
    void createsLabelWithSentimentAndConfidenceInExpectedRange() {
        SentimentLabel label = new RandomSentimentLabeler().label();

        assertNotNull(label.sentiment());
        assertTrue(label.confidence().compareTo(BigDecimal.valueOf(0.50)) >= 0);
        assertTrue(label.confidence().compareTo(BigDecimal.valueOf(0.96)) <= 0);
    }
}
