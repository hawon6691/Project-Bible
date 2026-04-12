package com.projectbible.shop.maven.jdbc.postgresql.common.api;
public record ApiError(String code,String message,Object details) {}
