package com.projectbible.post.maven.jdbc.postgresql.common.api;
public record ApiError(String code,String message,Object details) {}
