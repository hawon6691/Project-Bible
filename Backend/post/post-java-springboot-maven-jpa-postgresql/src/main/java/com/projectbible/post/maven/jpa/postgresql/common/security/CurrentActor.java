package com.projectbible.post.maven.jpa.postgresql.common.security;

public record CurrentActor(Long id, String email, String role, String subjectType) {
    public boolean isAdmin() {
        return "ADMIN".equals(role) && "admin".equals(subjectType);
    }

    public boolean isUser() {
        return "USER".equals(role) && "user".equals(subjectType);
    }
}
