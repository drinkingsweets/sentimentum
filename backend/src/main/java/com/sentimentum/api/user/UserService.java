package com.sentimentum.api.user;

import com.sentimentum.api.common.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return users.findAll().stream().map(UserDto::from).toList();
    }

    @Transactional
    public UserDto create(CreateUserRequest request) {
        return UserDto.from(users.save(new User(request.name(), request.email(), request.passwordHash())));
    }

    @Transactional(readOnly = true)
    public User getEntity(UUID id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));
    }
}
