package com.sentimentum.api.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ImportMetrics {

    private final MeterRegistry registry;

    public ImportMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordImportedMessages(String sourceType, int count) {
        if (count > 0) {
            counter("sentimentum_imported_messages_total", sourceType, "all").increment(count);
        }
    }

    public void recordCreatedLabels(String sourceType, String labelMode, int count) {
        if (count > 0) {
            counter("sentimentum_sentiment_labels_created_total", sourceType, labelMode).increment(count);
        }
    }

    private Counter counter(String name, String sourceType, String labelMode) {
        return Counter.builder(name)
                .tag("source_type", sourceType)
                .tag("label_mode", labelMode)
                .register(registry);
    }
}
