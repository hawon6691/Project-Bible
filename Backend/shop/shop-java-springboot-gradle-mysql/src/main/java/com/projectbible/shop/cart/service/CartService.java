package com.projectbible.shop.cart.service;

import com.projectbible.shop.cart.dto.CartDtos.CartItemResponseDto;
import com.projectbible.shop.cart.dto.CartDtos.UpsertCartItemDto;
import com.projectbible.shop.cart.entity.CartItemEntity;
import com.projectbible.shop.cart.repository.CartRepository;
import com.projectbible.shop.common.api.MessageResponse;
import com.projectbible.shop.common.exception.AppException;
import com.projectbible.shop.common.security.CurrentActor;
import com.projectbible.shop.product.entity.ProductEntity;
import com.projectbible.shop.product.entity.ProductImageEntity;
import com.projectbible.shop.product.entity.ProductOptionEntity;
import com.projectbible.shop.product.repository.ProductRepository;
import com.projectbible.shop.user.entity.UserEntity;
import com.projectbible.shop.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CartItemResponseDto> list(CurrentActor actor) {
        requireUser(actor);
        return cartRepository.findAllByUserId(actor.id()).stream().map(this::toResponse).toList();
    }

    public CartItemResponseDto create(CurrentActor actor, UpsertCartItemDto body) {
        requireUser(actor);
        if (body.productId() == null) {
            throw new AppException("VALIDATION_ERROR", "productId is required", HttpStatus.BAD_REQUEST);
        }
        int quantity = positiveQuantity(body.quantity());
        UserEntity user = userRepository.findActiveById(actor.id())
            .orElseThrow(() -> new AppException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        ProductEntity product = cartRepository.findUsableProduct(body.productId())
            .orElseThrow(() -> new AppException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
        ProductOptionEntity option = resolveOption(body.productId(), body.productOptionId());
        if (option != null && quantity > option.getStock()) {
            throw new AppException("OUT_OF_STOCK", "Stock is insufficient", HttpStatus.CONFLICT);
        }
        if (option == null && quantity > product.getStock()) {
            throw new AppException("OUT_OF_STOCK", "Stock is insufficient", HttpStatus.CONFLICT);
        }

        CartItemEntity item = cartRepository.findDuplicate(actor.id(), body.productId(), body.productOptionId())
            .map(existing -> {
                int nextQuantity = existing.getQuantity() + quantity;
                int available = existing.getProductOption() == null ? existing.getProduct().getStock() : existing.getProductOption().getStock();
                if (nextQuantity > available) {
                    throw new AppException("OUT_OF_STOCK", "Stock is insufficient", HttpStatus.CONFLICT);
                }
                existing.updateQuantity(nextQuantity);
                return existing;
            })
            .orElseGet(() -> new CartItemEntity(user, product, option, quantity));

        cartRepository.save(item);
        return toResponse(item);
    }

    public CartItemResponseDto update(CurrentActor actor, long cartItemId, UpsertCartItemDto body) {
        requireUser(actor);
        CartItemEntity item = cartRepository.findByIdAndUserId(cartItemId, actor.id())
            .orElseThrow(() -> new AppException("CART_ITEM_NOT_FOUND", "Cart item not found", HttpStatus.NOT_FOUND));
        int quantity = positiveQuantity(body.quantity());
        int available = item.getProductOption() == null ? item.getProduct().getStock() : item.getProductOption().getStock();
        if (quantity > available) {
            throw new AppException("OUT_OF_STOCK", "Stock is insufficient", HttpStatus.CONFLICT);
        }
        item.updateQuantity(quantity);
        cartRepository.save(item);
        return toResponse(item);
    }

    public MessageResponse remove(CurrentActor actor, long cartItemId) {
        requireUser(actor);
        CartItemEntity item = cartRepository.findByIdAndUserId(cartItemId, actor.id())
            .orElseThrow(() -> new AppException("CART_ITEM_NOT_FOUND", "Cart item not found", HttpStatus.NOT_FOUND));
        cartRepository.remove(item);
        return new MessageResponse("Cart item deleted successfully");
    }

    public MessageResponse clear(CurrentActor actor) {
        requireUser(actor);
        cartRepository.clearByUserId(actor.id());
        return new MessageResponse("Cart cleared successfully");
    }

    private ProductOptionEntity resolveOption(Long productId, Long productOptionId) {
        if (productOptionId == null) return null;
        return cartRepository.findOption(productId, productOptionId)
            .orElseThrow(() -> new AppException("PRODUCT_OPTION_NOT_FOUND", "Product option not found", HttpStatus.NOT_FOUND));
    }

    private CartItemResponseDto toResponse(CartItemEntity item) {
        ProductOptionEntity option = item.getProductOption();
        BigDecimal unitPrice = item.getProduct().getPrice().add(option == null ? BigDecimal.ZERO : option.getAdditionalPrice());
        String imageUrl = productRepository.findImages(item.getProduct().getId()).stream()
            .sorted((a, b) -> {
                int primary = Boolean.compare(b.isPrimary(), a.isPrimary());
                if (primary != 0) return primary;
                int order = Integer.compare(a.getDisplayOrder(), b.getDisplayOrder());
                return order != 0 ? order : Long.compare(a.getId(), b.getId());
            })
            .map(ProductImageEntity::getImageUrl)
            .findFirst()
            .orElse(null);
        return new CartItemResponseDto(
            item.getId(),
            item.getProduct().getId(),
            option == null ? null : option.getId(),
            item.getProduct().getName(),
            option == null ? null : option.getName() + ":" + option.getValue(),
            unitPrice,
            item.getQuantity(),
            imageUrl,
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }

    private int positiveQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new AppException("VALIDATION_ERROR", "quantity must be greater than 0", HttpStatus.BAD_REQUEST);
        }
        return quantity;
    }

    private void requireUser(CurrentActor actor) {
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
    }
}
