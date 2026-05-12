package com.sentimentum.api.project;

import com.sentimentum.api.common.NotFoundException;
import com.sentimentum.api.user.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projects;
    private final UserService users;

    public ProjectService(ProjectRepository projects, UserService users) {
        this.projects = projects;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> list() {
        return projects.findAll().stream().map(ProjectDto::from).toList();
    }

    @Transactional
    public ProjectDto create(CreateProjectRequest request) {
        Project project = new Project(request.name(), request.description(), users.getEntity(request.ownerId()));
        return ProjectDto.from(projects.save(project));
    }

    @Transactional(readOnly = true)
    public Project getEntity(UUID id) {
        return projects.findById(id).orElseThrow(() -> new NotFoundException("Project not found: " + id));
    }
}
