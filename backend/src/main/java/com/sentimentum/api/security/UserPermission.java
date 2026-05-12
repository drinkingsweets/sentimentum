package com.sentimentum.api.security;

import com.sentimentum.api.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "user_permissions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_permission", columnNames = {"user_id", "permission_id"})
})
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    protected UserPermission() {
    }

    public UserPermission(User user, Permission permission) {
        this.user = user;
        this.permission = permission;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Permission getPermission() {
        return permission;
    }
}
