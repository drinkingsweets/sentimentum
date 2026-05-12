package com.sentimentum.api.project;

import com.sentimentum.api.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users_projects", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_project", columnNames = {"user_id", "project_id"})
})
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "role_in_company", nullable = false)
    private String roleInCompany;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected UserProject() {
    }

    public UserProject(User user, Project project, String roleInCompany) {
        this.user = user;
        this.project = project;
        this.roleInCompany = roleInCompany;
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

    public String getRoleInCompany() {
        return roleInCompany;
    }

    public void setRoleInCompany(String roleInCompany) {
        this.roleInCompany = roleInCompany;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
