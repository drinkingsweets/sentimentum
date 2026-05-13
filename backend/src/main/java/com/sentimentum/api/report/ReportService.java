package com.sentimentum.api.report;

import com.sentimentum.api.project.ProjectService;
import com.sentimentum.api.user.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reports;
    private final UserService users;
    private final ProjectService projects;

    public ReportService(ReportRepository reports, UserService users, ProjectService projects) {
        this.reports = reports;
        this.users = users;
        this.projects = projects;
    }

    @Transactional(readOnly = true)
    public List<ReportDto> list(UUID projectId, UUID ownerId) {
        List<Report> result = projectId == null
                ? reports.findByProjectOwnerId(ownerId)
                : reports.findByProjectIdAndProjectOwnerId(projectId, ownerId);
        return result.stream().map(ReportDto::from).toList();
    }

    @Transactional
    public ReportDto create(CreateReportRequest request, UUID ownerId) {
        Report report = new Report(
                users.getEntity(ownerId),
                projects.getOwnedEntity(request.projectId(), ownerId),
                request.title(),
                request.data(),
                request.format()
        );
        return ReportDto.from(reports.save(report));
    }
}
