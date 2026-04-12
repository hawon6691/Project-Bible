package com.projectbible.shop.gradle.jpa.postgresql.common.api;
public record ApiError(String code,String message,Object details) {}
