package com.projectbible.post.maven.postgresql.user.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectbible.post.maven.postgresql.auth.repository.AuthRepository;
import com.projectbible.post.maven.postgresql.common.api.MessageResponse;
import com.projectbible.post.maven.postgresql.common.exception.AppException;
import com.projectbible.post.maven.postgresql.common.security.CurrentActor;
import com.projectbible.post.maven.postgresql.user.dto.UserDtos.UpdateUserDto;
import com.projectbible.post.maven.postgresql.user.dto.UserDtos.UserSummaryDto;
import com.projectbible.post.maven.postgresql.user.entity.UserEntity;
import com.projectbible.post.maven.postgresql.user.repository.UserRepository;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, AuthRepository authRepository) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
    }

    @Transactional(readOnly = true)
    public UserSummaryDto me(CurrentActor actor) {
        UserEntity user = userRepository.findActiveById(actor.id())
            .orElseThrow(() -> new AppException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        return toSummary(user);
    }

    public UserSummaryDto update(CurrentActor actor, UpdateUserDto body) {
        UserEntity user = userRepository.findActiveById(actor.id())
            .orElseThrow(() -> new AppException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        String passwordHash = body.password() == null || body.password().isBlank() ? null : encoder.encode(body.password());
        user.updateProfile(body.nickname().trim(), passwordHash);
        userRepository.save(user);
        return toSummary(user);
    }

    public MessageResponse remove(CurrentActor actor) {
        UserEntity user = userRepository.findActiveById(actor.id())
            .orElseThrow(() -> new AppException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        user.markDeleted();
        userRepository.save(user);
        authRepository.revokeAllUserTokens(actor.id());
        return new MessageResponse("User deleted successfully");
    }

    private UserSummaryDto toSummary(UserEntity user) {
        return new UserSummaryDto(user.getId(), user.getEmail(), user.getNickname(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
