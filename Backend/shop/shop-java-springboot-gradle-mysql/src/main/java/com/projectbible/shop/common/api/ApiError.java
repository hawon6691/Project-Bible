package com.projectbible.shop.common.api;
public record ApiError(String code,String message,Object details) {}
