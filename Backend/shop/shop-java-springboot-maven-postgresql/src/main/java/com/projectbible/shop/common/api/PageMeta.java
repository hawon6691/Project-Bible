package com.projectbible.shop.common.api;

public record PageMeta(int page, int limit, int totalCount, int totalPages) {
    public static PageMeta of(int page, int limit, long totalCount) {
        int safeLimit = Math.max(limit, 1);
        int totalPages = (int) Math.ceil(totalCount / (double) safeLimit);
        return new PageMeta(page, safeLimit, Math.toIntExact(totalCount), totalPages);
    }
}
