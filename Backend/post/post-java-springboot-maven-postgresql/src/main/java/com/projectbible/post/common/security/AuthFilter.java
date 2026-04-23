package com.projectbible.post.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.projectbible.post.common.exception.AppException;

@Component
public class AuthFilter extends OncePerRequestFilter {
    private static final String ATTR = "currentActor";
    private final TokenService tokenService;

    public AuthFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                request.setAttribute(ATTR, tokenService.parse(header.substring(7)));
            } catch (Exception ignored) {
                throw new AppException("UNAUTHORIZED", "Invalid access token", HttpStatus.UNAUTHORIZED);
            }
        }
        filterChain.doFilter(request, response);
    }

    public static CurrentActor actor(HttpServletRequest request) {
        return (CurrentActor) request.getAttribute(ATTR);
    }
}
