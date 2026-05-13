package com.sentimentum.api.message;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {

    List<AnalysisResult> findByMessageId(UUID messageId);

    List<AnalysisResult> findByMessageSourceProjectId(UUID projectId);

    List<AnalysisResult> findByMessageSourceProjectOwnerId(UUID ownerId);

    List<AnalysisResult> findByMessageIdAndMessageSourceProjectOwnerId(UUID messageId, UUID ownerId);

    List<AnalysisResult> findByMessageSourceProjectIdAndMessageSourceProjectOwnerId(UUID projectId, UUID ownerId);
}
