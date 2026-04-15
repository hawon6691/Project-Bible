package com.projectbible.shop.maven.jpa.postgresql.auth.service;

import com.projectbible.shop.maven.jpa.postgresql.auth.dto.AuthDtos.LoginRequestDto;
import com.projectbible.shop.maven.jpa.postgresql.auth.dto.AuthDtos.LoginResponseDto;
import com.projectbible.shop.maven.jpa.postgresql.auth.dto.AuthDtos.RefreshRequestDto;
import com.projectbible.shop.maven.jpa.postgresql.auth.dto.AuthDtos.SignupRequestDto;
import com.projectbible.shop.maven.jpa.postgresql.auth.entity.UserRefreshTokenEntity;
import com.projectbible.shop.maven.jpa.postgresql.auth.repository.AuthRepository;
import com.projectbible.shop.maven.jpa.postgresql.common.api.MessageResponse;
import com.projectbible.shop.maven.jpa.postgresql.common.exception.AppException;
import com.projectbible.shop.maven.jpa.postgresql.common.security.CurrentActor;
import com.projectbible.shop.maven.jpa.postgresql.common.security.TokenService;
import com.projectbible.shop.maven.jpa.postgresql.user.dto.UserDtos.UserSummaryDto;
import com.projectbible.shop.maven.jpa.postgresql.user.entity.UserEntity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final AuthRepository authRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(AuthRepository authRepository, TokenService tokenService) {
        this.authRepository = authRepository;
        this.tokenService = tokenService;
    }

    public UserSummaryDto signup(SignupRequestDto request) {
        String email = request.email().trim().toLowerCase();
        if (authRepository.existsUserByEmail(email)) {
            throw new AppException("DUPLICATE_EMAIL", "Email already exists", HttpStatus.CONFLICT);
        }
        UserEntity user = new UserEntity(email, encoder.encode(request.password()), request.name().trim(), request.phone().trim(), "ACTIVE");
        authRepository.saveUser(user);
        return toSummary(user);
    }

    public LoginResponseDto login(LoginRequestDto request) {
        UserEntity user = authRepository.findUserByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new AppException("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.UNAUTHORIZED));
        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            throw new AppException("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        return issueTokens(user);
    }

    public LoginResponseDto refresh(RefreshRequestDto request) {
        UserRefreshTokenEntity token = authRepository.findActiveRefreshToken(hash(request.refreshToken()))
            .orElseThrow(() -> new AppException("UNAUTHORIZED", "Invalid refresh token", HttpStatus.UNAUTHORIZED));
        token.revoke();
        authRepository.saveRefreshToken(token);
        return issueTokens(token.getUser());
    }

    public MessageResponse logout(CurrentActor actor) {
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
        authRepository.revokeAllUserTokens(actor.id());
        return new MessageResponse("Logged out successfully");
    }

    private LoginResponseDto issueTokens(UserEntity user) {
        CurrentActor actor = new CurrentActor(user.getId(), user.getEmail(), "USER", "user");
        String accessToken = tokenService.createAccessToken(actor, 1800);
        String refreshToken = UUID.randomUUID().toString();
        authRepository.saveRefreshToken(new UserRefreshTokenEntity(user, hash(refreshToken), LocalDateTime.now().plusDays(7)));
        return new LoginResponseDto(accessToken, refreshToken, 1800, toSummary(user));
    }

    private UserSummaryDto toSummary(UserEntity user) {
        return new UserSummaryDto(user.getId(), user.getEmail(), user.getName(), user.getPhone(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt());
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
