package com.projectbible.shop.review.service;

import com.projectbible.shop.common.api.MessageResponse;
import com.projectbible.shop.common.api.PageMeta;
import com.projectbible.shop.common.exception.AppException;
import com.projectbible.shop.common.security.CurrentActor;
import com.projectbible.shop.order.entity.OrderItemEntity;
import com.projectbible.shop.review.dto.ReviewDtos.AdminReviewSummaryDto;
import com.projectbible.shop.review.dto.ReviewDtos.ReviewDetailDto;
import com.projectbible.shop.review.dto.ReviewDtos.ReviewListItemDto;
import com.projectbible.shop.review.dto.ReviewDtos.UpsertReviewDto;
import com.projectbible.shop.review.entity.ReviewEntity;
import com.projectbible.shop.review.repository.ReviewRepository;
import com.projectbible.shop.user.entity.UserEntity;
import com.projectbible.shop.user.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PagedReviews list(long productId, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 20 : limit;
        long totalCount = reviewRepository.countByProductId(productId);
        List<ReviewListItemDto> items = reviewRepository.findByProductId(productId, safePage, safeLimit).stream()
            .map(this::toListItem)
            .toList();
        return new PagedReviews(items, PageMeta.of(safePage, safeLimit, totalCount));
    }

    @Transactional(readOnly = true)
    public ReviewDetailDto one(long reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new AppException("REVIEW_NOT_FOUND", "Review not found", HttpStatus.NOT_FOUND));
        return toDetail(review);
    }

    public ReviewDetailDto create(CurrentActor actor, long orderItemId, UpsertReviewDto body) {
        requireUser(actor);
        if (body == null || body.rating() == null) {
            throw new AppException("VALIDATION_ERROR", "rating is required", HttpStatus.BAD_REQUEST);
        }
        UserEntity user = userRepository.findActiveById(actor.id())
            .orElseThrow(() -> new AppException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        OrderItemEntity orderItem = reviewRepository.findReviewableOrderItem(orderItemId, actor.id())
            .orElseThrow(() -> new AppException("ORDER_ITEM_NOT_FOUND", "Order item not found", HttpStatus.NOT_FOUND));
        if (reviewRepository.findByOrderItemId(orderItemId).isPresent()) {
            throw new AppException("REVIEW_ALREADY_EXISTS", "Review already exists", HttpStatus.CONFLICT);
        }
        ReviewEntity review = new ReviewEntity(orderItem, orderItem.getProduct(), user, body.rating(), trimToNull(body.content()));
        reviewRepository.save(review);
        return toDetail(review);
    }

    public ReviewDetailDto update(CurrentActor actor, long reviewId, UpsertReviewDto body) {
        requireUser(actor);
        ReviewEntity review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new AppException("REVIEW_NOT_FOUND", "Review not found", HttpStatus.NOT_FOUND));
        if (!review.getUser().getId().equals(actor.id())) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
        Integer rating = body == null || body.rating() == null ? review.getRating() : body.rating();
        review.update(rating, body == null ? review.getContent() : trimToNull(body.content()));
        reviewRepository.save(review);
        return toDetail(review);
    }

    public MessageResponse remove(CurrentActor actor, long reviewId) {
        requireUser(actor);
        ReviewEntity review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new AppException("REVIEW_NOT_FOUND", "Review not found", HttpStatus.NOT_FOUND));
        if (!review.getUser().getId().equals(actor.id())) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
        review.markDeleted();
        reviewRepository.save(review);
        return new MessageResponse("Review deleted successfully");
    }

    @Transactional(readOnly = true)
    public PagedAdminReviews adminList(String search, String status, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 20 : limit;
        long totalCount = reviewRepository.countAdmin(search, status);
        List<AdminReviewSummaryDto> items = reviewRepository.findAdmin(safePage, safeLimit, search, status).stream()
            .map(review -> new AdminReviewSummaryDto(
                review.getId(),
                review.getProduct().getId(),
                review.getProduct().getName(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getRating(),
                review.getStatus(),
                review.getCreatedAt()
            ))
            .toList();
        return new PagedAdminReviews(items, PageMeta.of(safePage, safeLimit, totalCount));
    }

    public MessageResponse adminRemove(long reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new AppException("REVIEW_NOT_FOUND", "Review not found", HttpStatus.NOT_FOUND));
        review.markDeleted();
        reviewRepository.save(review);
        return new MessageResponse("Review deleted successfully");
    }

    private ReviewListItemDto toListItem(ReviewEntity review) {
        return new ReviewListItemDto(
            review.getId(),
            review.getProduct().getId(),
            review.getUser().getId(),
            review.getUser().getName(),
            review.getRating(),
            review.getContent(),
            review.getStatus(),
            review.getCreatedAt(),
            review.getUpdatedAt()
        );
    }

    private ReviewDetailDto toDetail(ReviewEntity review) {
        return new ReviewDetailDto(
            review.getId(),
            review.getOrderItem().getId(),
            review.getProduct().getId(),
            review.getProduct().getName(),
            review.getUser().getId(),
            review.getUser().getName(),
            review.getRating(),
            review.getContent(),
            review.getStatus(),
            review.getCreatedAt(),
            review.getUpdatedAt()
        );
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void requireUser(CurrentActor actor) {
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
    }

    public record PagedReviews(List<ReviewListItemDto> items, PageMeta meta) {}
    public record PagedAdminReviews(List<AdminReviewSummaryDto> items, PageMeta meta) {}
}
