package com.sentimentum.api.message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findBySourceId(UUID sourceId);

    List<Message> findBySourceProjectOwnerId(UUID ownerId);

    List<Message> findBySourceIdAndSourceProjectOwnerId(UUID sourceId, UUID ownerId);

    Optional<Message> findByIdAndSourceProjectOwnerId(UUID id, UUID ownerId);
}
