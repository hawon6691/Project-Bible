package com.projectbible.post.maven.postgresql.admin.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectbible.post.maven.postgresql.admin.dto.AdminDtos.AdminDashboardResponseDto;
import com.projectbible.post.maven.postgresql.admin.dto.AdminDtos.AdminLoginRequestDto;
import com.projectbible.post.maven.postgresql.admin.dto.AdminDtos.AdminLoginResponseDto;
import com.projectbible.post.maven.postgresql.admin.dto.AdminDtos.AdminRefreshRequestDto;
import com.projectbible.post.maven.postgresql.admin.dto.AdminDtos.AdminSummaryDto;
import com.projectbible.post.maven.postgresql.admin.entity.AdminEntity;
import com.projectbible.post.maven.postgresql.admin.entity.AdminRefreshTokenEntity;
import com.projectbible.post.maven.postgresql.admin.repository.AdminRepository;
import com.projectbible.post.maven.postgresql.common.api.MessageResponse;
import com.projectbible.post.maven.postgresql.common.exception.AppException;
import com.projectbible.post.maven.postgresql.common.security.CurrentActor;
import com.projectbible.post.maven.postgresql.common.security.TokenService;

@Service
@Transactional
public class AdminService {
    private final AdminRepository adminRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AdminService(AdminRepository adminRepository, TokenService tokenService) {
        this.adminRepository = adminRepository;
        this.tokenService = tokenService;
    }

    public AdminLoginResponseDto login(AdminLoginRequestDto request) {
        AdminEntity admin = adminRepository.findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new AppException("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.UNAUTHORIZED));
        if (!encoder.matches(request.password(), admin.getPasswordHash())) {
            throw new AppException("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        return issueTokens(admin);
    }

    public AdminLoginResponseDto refresh(AdminRefreshRequestDto request) {
        AdminRefreshTokenEntity token = adminRepository.findActiveRefreshToken(hash(request.refreshToken()))
            .orElseThrow(() -> new AppException("UNAUTHORIZED", "Invalid refresh token", HttpStatus.UNAUTHORIZED));
        token.revoke();
        adminRepository.saveRefreshToken(token);
        return issueTokens(token.getAdmin());
    }

    public MessageResponse logout(CurrentActor actor) {
        if (!actor.isAdmin()) {
            throw new AppException("FORBIDDEN", "Admin access required", HttpStatus.FORBIDDEN);
        }
        adminRepository.revokeAllAdminTokens(actor.id());
        return new MessageResponse("Logged out successfully");
    }

    @Transactional(readOnly = true)
    public AdminSummaryDto me(CurrentActor actor) {
        if (!actor.isAdmin()) {
            throw new AppException("FORBIDDEN", "Admin access required", HttpStatus.FORBIDDEN);
        }
        AdminEntity admin = adminRepository.findById(actor.id())
            .orElseThrow(() -> new AppException("ADMIN_NOT_FOUND", "Admin not found", HttpStatus.NOT_FOUND));
        return toSummary(admin);
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponseDto dashboard() {
        return adminRepository.dashboard();
    }

    private AdminLoginResponseDto issueTokens(AdminEntity admin) {
        CurrentActor actor = new CurrentActor(admin.getId(), admin.getEmail(), "ADMIN", "admin");
        String accessToken = tokenService.createAccessToken(actor, 1800);
        String refreshToken = UUID.randomUUID().toString();
        adminRepository.saveRefreshToken(new AdminRefreshTokenEntity(admin, hash(refreshToken), LocalDateTime.now().plusDays(7)));
        return new AdminLoginResponseDto(accessToken, refreshToken, 1800, toSummary(admin));
    }

    private AdminSummaryDto toSummary(AdminEntity admin) {
        return new AdminSummaryDto(admin.getId(), admin.getEmail(), admin.getName(), admin.getStatus(), admin.getCreatedAt());
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
