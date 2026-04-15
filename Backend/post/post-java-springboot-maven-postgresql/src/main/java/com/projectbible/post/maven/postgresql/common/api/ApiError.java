package com.projectbible.post.maven.postgresql.common.api;
public record ApiError(String code,String message,Object details) {}
