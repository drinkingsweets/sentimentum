package com.sentimentum.api.report;

import com.sentimentum.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReportDto> list(@AuthenticationPrincipal AuthenticatedUser user, @RequestParam(required = false) UUID projectId) {
        return service.list(projectId, user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportDto create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateReportRequest request) {
        return service.create(request, user.getId());
    }
}
