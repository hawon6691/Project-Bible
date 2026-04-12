package com.projectbible.post.maven.jpa.postgresql.common.api;
public record ApiError(String code,String message,Object details) {}
