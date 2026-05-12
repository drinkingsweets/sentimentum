package com.sentimentum.api.project;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProjectRepository extends JpaRepository<UserProject, UUID> {
}
