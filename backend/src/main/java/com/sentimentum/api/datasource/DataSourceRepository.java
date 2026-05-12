package com.sentimentum.api.datasource;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSourceRepository extends JpaRepository<DataSource, UUID> {

    List<DataSource> findByProjectId(UUID projectId);
}
