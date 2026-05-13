package com.sentimentum.api.datasource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSourceRepository extends JpaRepository<DataSource, UUID> {

    List<DataSource> findByProjectId(UUID projectId);

    List<DataSource> findByProjectOwnerId(UUID ownerId);

    List<DataSource> findByProjectIdAndProjectOwnerId(UUID projectId, UUID ownerId);

    Optional<DataSource> findByIdAndProjectOwnerId(UUID id, UUID ownerId);
}
