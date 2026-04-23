package com.projectbible.shop.common.security;

import com.projectbible.shop.common.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

public final class AuthContext {
    private AuthContext() {}

    public static CurrentActor requireUser(HttpServletRequest request) {
        CurrentActor actor = AuthFilter.actor(request);
        if (actor == null) {
            throw new AppException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED);
        }
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
        return actor;
    }

    public static CurrentActor requireAdmin(HttpServletRequest request) {
        CurrentActor actor = AuthFilter.actor(request);
        if (actor == null) {
            throw new AppException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED);
        }
        if (!actor.isAdmin()) {
            throw new AppException("FORBIDDEN", "Admin access required", HttpStatus.FORBIDDEN);
        }
        return actor;
    }
}
