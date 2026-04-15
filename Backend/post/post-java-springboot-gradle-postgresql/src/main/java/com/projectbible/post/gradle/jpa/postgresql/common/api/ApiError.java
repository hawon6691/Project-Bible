package com.projectbible.post.gradle.jpa.postgresql.common.api;
public record ApiError(String code,String message,Object details) {}
