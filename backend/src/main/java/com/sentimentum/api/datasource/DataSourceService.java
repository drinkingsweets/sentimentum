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
    public List<DataSourceDto> list(UUID projectId) {
        List<DataSource> result = projectId == null ? sources.findAll() : sources.findByProjectId(projectId);
        return result.stream().map(DataSourceDto::from).toList();
    }

    @Transactional
    public DataSourceDto create(CreateDataSourceRequest request) {
        DataSource source = new DataSource(
                request.name(),
                request.link(),
                request.type(),
                projects.getEntity(request.projectId())
        );
        return DataSourceDto.from(sources.save(source));
    }

    @Transactional(readOnly = true)
    public DataSource getEntity(UUID id) {
        return sources.findById(id).orElseThrow(() -> new NotFoundException("Data source not found: " + id));
    }
}
