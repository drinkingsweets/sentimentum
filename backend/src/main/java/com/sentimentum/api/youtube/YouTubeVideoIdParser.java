package com.sentimentum.api.youtube;

import java.net.URI;
import java.util.Arrays;

final class YouTubeVideoIdParser {

    private YouTubeVideoIdParser() {
    }

    static String parse(String video) {
        String value = video.trim();
        if (!value.contains("://")) {
            return value;
        }

        URI uri = URI.create(value);
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
        String path = uri.getPath() == null ? "" : uri.getPath();

        if (host.contains("youtu.be")) {
            return firstPathSegment(path);
        }
        if (path.startsWith("/shorts/") || path.startsWith("/embed/")) {
            return segment(path, 1);
        }
        if (path.equals("/watch")) {
            return queryParam(uri.getQuery(), "v");
        }
        throw new IllegalArgumentException("Unsupported YouTube video URL: " + video);
    }

    private static String firstPathSegment(String path) {
        return segment(path, 0);
    }

    private static String segment(String path, int index) {
        String[] segments = Arrays.stream(path.split("/"))
                .filter(part -> !part.isBlank())
                .toArray(String[]::new);
        if (segments.length <= index) {
            throw new IllegalArgumentException("YouTube video id is missing");
        }
        return segments[index];
    }

    private static String queryParam(String query, String name) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("YouTube video id is missing");
        }
        return Arrays.stream(query.split("&"))
                .map(part -> part.split("=", 2))
                .filter(pair -> pair.length == 2 && pair[0].equals(name))
                .map(pair -> pair[1])
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("YouTube video id is missing"));
    }
}
