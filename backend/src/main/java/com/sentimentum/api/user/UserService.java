package com.sentimentum.api.user;

import com.sentimentum.api.common.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return users.findAll().stream().map(UserDto::from).toList();
    }

    @Transactional(readOnly = true)
    public UserDto get(UUID id) {
        return UserDto.from(getEntity(id));
    }

    @Transactional
    public UserDto create(CreateUserRequest request) {
        String passwordHash = passwordEncoder.encode(request.password());
        return UserDto.from(users.save(new User(request.name(), request.email(), passwordHash)));
    }

    @Transactional(readOnly = true)
    public User getEntity(UUID id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));
    }
}
