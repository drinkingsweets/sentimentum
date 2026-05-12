package com.sentimentum.api.youtube;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class YouTubeCommentsClient {

    private final RestClient restClient;
    private final String apiKey;

    public YouTubeCommentsClient(RestClient.Builder restClientBuilder, @Value("${youtube.api-key}") String apiKey) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
    }

    public List<YouTubeComment> fetchComments(String videoId, int maxResults) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("YOUTUBE_API_KEY is not configured");
        }

        String url = UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/youtube/v3/commentThreads")
                .queryParam("part", "snippet")
                .queryParam("videoId", videoId)
                .queryParam("maxResults", maxResults)
                .queryParam("textFormat", "plainText")
                .queryParam("key", apiKey)
                .toUriString();

        JsonNode root = restClient.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);

        List<YouTubeComment> comments = new ArrayList<>();
        if (root == null || !root.has("items")) {
            return comments;
        }

        for (JsonNode item : root.get("items")) {
            JsonNode snippet = item.path("snippet").path("topLevelComment").path("snippet");
            comments.add(new YouTubeComment(
                    snippet.path("textDisplay").asText(),
                    snippet.path("authorDisplayName").asText(null),
                    Instant.parse(snippet.path("publishedAt").asText())
            ));
        }
        return comments;
    }
}
