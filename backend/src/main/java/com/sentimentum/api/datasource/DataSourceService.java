package com.sentimentum.api.datasource;

import com.sentimentum.api.common.NotFoundException;
import com.sentimentum.api.project.ProjectService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataSourceService {

    private final DataSourceRepository sources;
    private final ProjectService projects;

    public DataSourceService(DataSourceRepository sources, ProjectService projects) {
        this.sources = sources;
        this.projects = projects;
    }

    @Transactional(readOnly = true)
    public List<DataSourceDto> list(UUID projectId, UUID ownerId) {
        List<DataSource> result = projectId == null
                ? sources.findByProjectOwnerId(ownerId)
                : sources.findByProjectIdAndProjectOwnerId(projectId, ownerId);
        return result.stream().map(DataSourceDto::from).toList();
    }

    @Transactional
    public DataSourceDto create(CreateDataSourceRequest request, UUID ownerId) {
        DataSource source = new DataSource(
                request.name(),
                request.link(),
                request.type(),
                projects.getOwnedEntity(request.projectId(), ownerId)
        );
        return DataSourceDto.from(sources.save(source));
    }

    @Transactional(readOnly = true)
    public DataSource getEntity(UUID id) {
        return sources.findById(id).orElseThrow(() -> new NotFoundException("Data source not found: " + id));
    }

    @Transactional(readOnly = true)
    public DataSource getOwnedEntity(UUID id, UUID ownerId) {
        return sources.findByIdAndProjectOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("Data source not found: " + id));
    }
}
