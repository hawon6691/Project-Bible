package com.projectbible.shop.maven.jpa.postgresql.common.api;
public record ApiError(String code,String message,Object details) {}
