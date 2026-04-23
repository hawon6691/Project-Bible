package com.projectbible.shop.user.service;

import com.projectbible.shop.auth.repository.AuthRepository;
import com.projectbible.shop.common.api.MessageResponse;
import com.projectbible.shop.common.exception.AppException;
import com.projectbible.shop.common.security.CurrentActor;
import com.projectbible.shop.user.dto.UserDtos.UpdateUserDto;
import com.projectbible.shop.user.dto.UserDtos.UserSummaryDto;
import com.projectbible.shop.user.entity.UserEntity;
import com.projectbible.shop.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final AuthRepository authRepository;

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
        user.updateProfile(
            body.name() == null || body.name().isBlank() ? user.getName() : body.name().trim(),
            body.phone() == null || body.phone().isBlank() ? user.getPhone() : body.phone().trim()
        );
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
        return new UserSummaryDto(user.getId(), user.getEmail(), user.getName(), user.getPhone(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
