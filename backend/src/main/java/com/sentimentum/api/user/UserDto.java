package com.sentimentum.api.user;

import java.time.Instant;
import java.util.UUID;

public record UserDto(UUID id, String name, String email, Instant createdAt) {

    static UserDto from(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}
