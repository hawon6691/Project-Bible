package com.projectbible.shop.product.service;

import com.projectbible.shop.category.entity.CategoryEntity;
import com.projectbible.shop.category.repository.CategoryRepository;
import com.projectbible.shop.common.api.MessageResponse;
import com.projectbible.shop.common.api.PageMeta;
import com.projectbible.shop.common.exception.AppException;
import com.projectbible.shop.product.dto.ProductDtos.*;
import com.projectbible.shop.product.entity.ProductEntity;
import com.projectbible.shop.product.entity.ProductImageEntity;
import com.projectbible.shop.product.entity.ProductOptionEntity;
import com.projectbible.shop.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductQueryService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductQueryService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public PagedProducts list(Integer page, Integer limit, Long categoryId, String search, String sort, String status) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 20 : limit;
        long totalCount = productRepository.countAll(categoryId, search, status, false);
        List<ProductListItemDto> items = productRepository.findAll(safePage, safeLimit, categoryId, search, sort, status, false)
            .stream()
            .map(product -> new ProductListItemDto(
                product.getId(),
                product.getCategory().getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getStatus(),
                productRepository.findImages(product.getId()).stream().sorted((a, b) -> {
                    int primary = Boolean.compare(b.isPrimary(), a.isPrimary());
                    if (primary != 0) return primary;
                    int order = Integer.compare(a.getDisplayOrder(), b.getDisplayOrder());
                    return order != 0 ? order : Long.compare(a.getId(), b.getId());
                }).map(ProductImageEntity::getImageUrl).findFirst().orElse(null)
            ))
            .toList();
        return new PagedProducts(items, PageMeta.of(safePage, safeLimit, totalCount));
    }

    @Transactional(readOnly = true)
    public ProductDetailDto one(long productId) {
        ProductEntity product = productRepository.findDetail(productId)
            .orElseThrow(() -> new AppException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
        return toDetail(product);
    }

    public ProductDetailDto create(UpsertProductDto body) {
        CategoryEntity category = usableCategory(body.categoryId());
        ProductEntity product = new ProductEntity(
            category,
            body.name().trim(),
            trimToNull(body.description()),
            body.price(),
            body.stock() == null ? 0 : body.stock(),
            body.status() == null || body.status().isBlank() ? "ACTIVE" : body.status().trim().toUpperCase()
        );
        productRepository.save(product);
        return toDetail(product);
    }

    public ProductDetailDto update(long productId, UpsertProductDto body) {
        ProductEntity product = productRepository.findDetail(productId)
            .orElseThrow(() -> new AppException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
        CategoryEntity category = body.categoryId() == null ? product.getCategory() : usableCategory(body.categoryId());
        product.update(
            category,
            body.name() == null || body.name().isBlank() ? product.getName() : body.name().trim(),
            body.description() == null ? product.getDescription() : trimToNull(body.description()),
            body.price() == null ? product.getPrice() : body.price(),
            body.stock() == null ? product.getStock() : body.stock(),
            body.status() == null || body.status().isBlank() ? product.getStatus() : body.status().trim().toUpperCase()
        );
        productRepository.save(product);
        return toDetail(product);
    }

    public MessageResponse remove(long productId) {
        ProductEntity product = productRepository.findDetail(productId)
            .orElseThrow(() -> new AppException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
        product.markDeleted();
        productRepository.save(product);
        return new MessageResponse("Product deleted successfully");
    }

    public ProductOptionDto createOption(long productId, UpsertProductOptionDto body) {
        ProductEntity product = productRepository.findDetail(productId)
            .orElseThrow(() -> new AppException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
        ProductOptionEntity option = new ProductOptionEntity(product, body.name().trim(), body.value().trim(), defaultMoney(body.additionalPrice()), body.stock() == null ? 0 : body.stock());
        productRepository.saveOption(option);
        return toOption(option);
    }

    public ProductOptionDto updateOption(long optionId, UpsertProductOptionDto body) {
        ProductOptionEntity option = productRepository.findOption(optionId)
            .orElseThrow(() -> new AppException("PRODUCT_OPTION_NOT_FOUND", "Product option not found", HttpStatus.NOT_FOUND));
        option.update(
            body.name() == null || body.name().isBlank() ? option.getName() : body.name().trim(),
            body.value() == null || body.value().isBlank() ? option.getValue() : body.value().trim(),
            body.additionalPrice() == null ? option.getAdditionalPrice() : body.additionalPrice(),
            body.stock() == null ? option.getStock() : body.stock()
        );
        productRepository.saveOption(option);
        return toOption(option);
    }

    public MessageResponse removeOption(long optionId) {
        ProductOptionEntity option = productRepository.findOption(optionId)
            .orElseThrow(() -> new AppException("PRODUCT_OPTION_NOT_FOUND", "Product option not found", HttpStatus.NOT_FOUND));
        productRepository.removeOption(option);
        return new MessageResponse("Product option deleted successfully");
    }

    public ProductImageDto createImage(long productId, UpsertProductImageDto body) {
        ProductEntity product = productRepository.findDetail(productId)
            .orElseThrow(() -> new AppException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
        ProductImageEntity image = new ProductImageEntity(product, body.imageUrl().trim(), body.isPrimary() != null && body.isPrimary(), body.displayOrder() == null ? 0 : body.displayOrder());
        productRepository.saveImage(image);
        return toImage(image);
    }

    public ProductImageDto updateImage(long imageId, UpsertProductImageDto body) {
        ProductImageEntity image = productRepository.findImage(imageId)
            .orElseThrow(() -> new AppException("PRODUCT_NOT_FOUND", "Product image not found", HttpStatus.NOT_FOUND));
        image.update(
            body.imageUrl() == null || body.imageUrl().isBlank() ? image.getImageUrl() : body.imageUrl().trim(),
            body.isPrimary() == null ? image.isPrimary() : body.isPrimary(),
            body.displayOrder() == null ? image.getDisplayOrder() : body.displayOrder()
        );
        productRepository.saveImage(image);
        return toImage(image);
    }

    public MessageResponse removeImage(long imageId) {
        ProductImageEntity image = productRepository.findImage(imageId)
            .orElseThrow(() -> new AppException("PRODUCT_NOT_FOUND", "Product image not found", HttpStatus.NOT_FOUND));
        productRepository.removeImage(image);
        return new MessageResponse("Product image deleted successfully");
    }

    private ProductDetailDto toDetail(ProductEntity product) {
        List<ProductOptionDto> options = productRepository.findOptions(product.getId()).stream().map(this::toOption).toList();
        List<ProductImageDto> images = productRepository.findImages(product.getId()).stream().map(this::toImage).toList();
        return new ProductDetailDto(
            product.getId(),
            product.getCategory().getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getStatus(),
            product.getCreatedAt(),
            product.getUpdatedAt(),
            options,
            images
        );
    }

    private ProductOptionDto toOption(ProductOptionEntity option) {
        return new ProductOptionDto(option.getId(), option.getProduct().getId(), option.getName(), option.getValue(), option.getAdditionalPrice(), option.getStock(), option.getCreatedAt(), option.getUpdatedAt());
    }

    private ProductImageDto toImage(ProductImageEntity image) {
        return new ProductImageDto(image.getId(), image.getProduct().getId(), image.getImageUrl(), image.isPrimary(), image.getDisplayOrder(), image.getCreatedAt(), image.getUpdatedAt());
    }

    private CategoryEntity usableCategory(Long categoryId) {
        return categoryRepository.findUsableById(categoryId)
            .orElseThrow(() -> new AppException("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND));
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record PagedProducts(List<ProductListItemDto> items, PageMeta meta) {}
}
