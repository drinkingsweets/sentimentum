package com.sentimentum.api.csv;

import com.sentimentum.api.datasource.DataSource;
import com.sentimentum.api.datasource.DataSourceRepository;
import com.sentimentum.api.datasource.DataSourceType;
import com.sentimentum.api.labeling.RandomSentimentLabeler;
import com.sentimentum.api.labeling.SentimentLabel;
import com.sentimentum.api.message.AnalysisResult;
import com.sentimentum.api.message.AnalysisResultRepository;
import com.sentimentum.api.message.Message;
import com.sentimentum.api.message.MessageRepository;
import com.sentimentum.api.message.Sentiment;
import com.sentimentum.api.metrics.ImportMetrics;
import com.sentimentum.api.project.Project;
import com.sentimentum.api.project.ProjectService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CsvMessagesImportService {

    private final ProjectService projects;
    private final DataSourceRepository sources;
    private final MessageRepository messages;
    private final AnalysisResultRepository results;
    private final RandomSentimentLabeler labeler;
    private final ImportMetrics metrics;

    public CsvMessagesImportService(
            ProjectService projects,
            DataSourceRepository sources,
            MessageRepository messages,
            AnalysisResultRepository results,
            RandomSentimentLabeler labeler,
            ImportMetrics metrics
    ) {
        this.projects = projects;
        this.sources = sources;
        this.messages = messages;
        this.results = results;
        this.labeler = labeler;
        this.metrics = metrics;
    }

    @Transactional
    public ImportCsvMessagesResponse importMessages(UUID projectId, MultipartFile file, UUID ownerId) {
        Project project = projects.getOwnedEntity(projectId, ownerId);
        DataSource source = sources.save(new DataSource(
                "CSV " + file.getOriginalFilename(),
                file.getOriginalFilename() == null ? "uploaded.csv" : file.getOriginalFilename(),
                DataSourceType.CSV,
                project
        ));

        int imported = 0;
        int randomLabeled = 0;

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)
        ) {
            for (CSVRecord record : parser) {
                Message message = messages.save(new Message(
                        required(record, "content"),
                        optional(record, "author"),
                        source,
                        valueOrDefault(record, "language", "unknown"),
                        valueOrDefault(record, "tag", "csv"),
                        parseCreatedAt(optional(record, "createdAt"))
                ));

                SentimentLabel label = labelFromCsv(record);
                if (label == null) {
                    label = labeler.label();
                    randomLabeled++;
                }
                results.save(new AnalysisResult(message, label.sentiment(), label.confidence()));
                imported++;
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot read CSV file", ex);
        }

        metrics.recordImportedMessages("csv", imported);
        metrics.recordCreatedLabels("csv", "provided", imported - randomLabeled);
        metrics.recordCreatedLabels("csv", "random", randomLabeled);

        return new ImportCsvMessagesResponse(source.getId(), imported, randomLabeled);
    }

    private SentimentLabel labelFromCsv(CSVRecord record) {
        String sentimentValue = optional(record, "sentiment");
        String confidenceValue = optional(record, "confidence");
        if (sentimentValue == null || confidenceValue == null) {
            return null;
        }
        return new SentimentLabel(Sentiment.valueOf(sentimentValue), new BigDecimal(confidenceValue));
    }

    private Instant parseCreatedAt(String value) {
        return value == null ? Instant.now() : Instant.parse(value);
    }

    private String valueOrDefault(CSVRecord record, String column, String fallback) {
        String value = optional(record, column);
        return value == null ? fallback : value;
    }

    private String required(CSVRecord record, String column) {
        String value = optional(record, column);
        if (value == null) {
            throw new IllegalArgumentException("CSV column is required: " + column);
        }
        return value;
    }

    private String optional(CSVRecord record, String column) {
        if (!record.isMapped(column) || !record.isSet(column)) {
            return null;
        }
        String value = record.get(column);
        return value == null || value.isBlank() ? null : value;
    }
}
