package com.sentimentum.api.report;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByProjectId(UUID projectId);

    List<Report> findByProjectOwnerId(UUID ownerId);

    List<Report> findByProjectIdAndProjectOwnerId(UUID projectId, UUID ownerId);
}
