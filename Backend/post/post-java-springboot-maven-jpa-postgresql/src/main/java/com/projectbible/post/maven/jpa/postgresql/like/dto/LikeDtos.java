package com.projectbible.post.maven.jpa.postgresql.like.dto;

public final class LikeDtos {
    private LikeDtos() {
    }

    public record LikeSummaryDto(Long postId, boolean liked, int likeCount) {}
}
