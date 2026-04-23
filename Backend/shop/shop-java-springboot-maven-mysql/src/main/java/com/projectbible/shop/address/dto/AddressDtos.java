package com.projectbible.shop.address.dto;

import java.time.LocalDateTime;

public final class AddressDtos {
    private AddressDtos() {}

    public record AddressResponseDto(
        Long id,
        Long userId,
        String recipientName,
        String phone,
        String zipCode,
        String address1,
        String address2,
        boolean isDefault,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record UpsertAddressDto(
        String recipientName,
        String phone,
        String zipCode,
        String address1,
        String address2,
        Boolean isDefault
    ) {}
}
