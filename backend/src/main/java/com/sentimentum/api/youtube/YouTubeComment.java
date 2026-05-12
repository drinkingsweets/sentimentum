package com.sentimentum.api.youtube;

import java.time.Instant;

record YouTubeComment(String content, String author, Instant createdAt) {
}
