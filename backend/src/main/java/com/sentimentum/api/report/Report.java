package com.sentimentum.api.report;

import com.sentimentum.api.project.Project;
import com.sentimentum.api.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportFormat format;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Report() {
    }

    public Report(User user, Project project, String title, String data, ReportFormat format) {
        this.user = user;
        this.project = project;
        this.title = title;
        this.data = data;
        this.format = format;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Project getProject() {
        return project;
    }

    public String getTitle() {
        return title;
    }

    public String getData() {
        return data;
    }

    public ReportFormat getFormat() {
        return format;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
