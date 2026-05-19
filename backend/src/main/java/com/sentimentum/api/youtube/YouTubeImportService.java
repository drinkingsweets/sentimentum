package com.sentimentum.api.youtube;

import com.sentimentum.api.datasource.DataSource;
import com.sentimentum.api.datasource.DataSourceRepository;
import com.sentimentum.api.datasource.DataSourceType;
import com.sentimentum.api.labeling.RandomSentimentLabeler;
import com.sentimentum.api.labeling.SentimentLabel;
import com.sentimentum.api.message.AnalysisResult;
import com.sentimentum.api.message.AnalysisResultRepository;
import com.sentimentum.api.message.Message;
import com.sentimentum.api.message.MessageRepository;
import com.sentimentum.api.project.Project;
import com.sentimentum.api.project.ProjectService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class YouTubeImportService {

    private final ProjectService projects;
    private final DataSourceRepository sources;
    private final MessageRepository messages;
    private final AnalysisResultRepository results;
    private final YouTubeCommentsClient commentsClient;
    private final RandomSentimentLabeler labeler;

    public YouTubeImportService(
            ProjectService projects,
            DataSourceRepository sources,
            MessageRepository messages,
            AnalysisResultRepository results,
            YouTubeCommentsClient commentsClient,
            RandomSentimentLabeler labeler
    ) {
        this.projects = projects;
        this.sources = sources;
        this.messages = messages;
        this.results = results;
        this.commentsClient = commentsClient;
        this.labeler = labeler;
    }

    @Transactional
    public ImportYouTubeCommentsResponse importComments(ImportYouTubeCommentsRequest request, UUID ownerId) {
        Project project = projects.getOwnedEntity(request.projectId(), ownerId);
        String videoId = YouTubeVideoIdParser.parse(request.video());
        int limit = request.maxResults() == null ? 50 : request.maxResults();

        DataSource source = sources.save(new DataSource(
                "YouTube video " + videoId,
                request.video(),
                DataSourceType.YOUTUBE,
                project
        ));

        List<Message> imported = commentsClient.fetchComments(videoId, limit).stream()
                .map(comment -> new Message(
                        comment.content(),
                        comment.author(),
                        source,
                        "unknown",
                        "youtube",
                        comment.createdAt()
                ))
                .toList();
        messages.saveAll(imported).forEach(message -> {
            SentimentLabel label = labeler.label();
            results.save(new AnalysisResult(message, label.sentiment(), label.confidence()));
        });

        return new ImportYouTubeCommentsResponse(source.getId(), videoId, imported.size());
    }
}
