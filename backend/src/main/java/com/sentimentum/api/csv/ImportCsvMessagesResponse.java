package com.sentimentum.api.csv;

import java.util.UUID;

public record ImportCsvMessagesResponse(UUID sourceId, int importedCount, int randomLabeledCount) {
}
