package com.projectbible.shop.gradle.jdbc.postgresql.common.api;
public record ApiError(String code,String message,Object details) {}
