package com.sentimentum.api.youtube;

import java.util.UUID;

public record ImportYouTubeCommentsResponse(UUID sourceId, String videoId, int importedCount) {
}
