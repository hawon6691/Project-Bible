package com.projectbible.shop.address.service;

import com.projectbible.shop.address.dto.AddressDtos.AddressResponseDto;
import com.projectbible.shop.address.dto.AddressDtos.UpsertAddressDto;
import com.projectbible.shop.address.entity.AddressEntity;
import com.projectbible.shop.address.repository.AddressRepository;
import com.projectbible.shop.common.api.MessageResponse;
import com.projectbible.shop.common.exception.AppException;
import com.projectbible.shop.common.security.CurrentActor;
import com.projectbible.shop.user.entity.UserEntity;
import com.projectbible.shop.user.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressResponseDto> list(CurrentActor actor) {
        requireUser(actor);
        return addressRepository.findAllByUserId(actor.id()).stream().map(this::toResponse).toList();
    }

    public AddressResponseDto create(CurrentActor actor, UpsertAddressDto body) {
        requireUser(actor);
        UserEntity user = userRepository.findActiveById(actor.id())
            .orElseThrow(() -> new AppException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        boolean isDefault = Boolean.TRUE.equals(body.isDefault());
        if (isDefault) {
            addressRepository.clearDefaultByUserId(actor.id());
        }
        AddressEntity address = new AddressEntity(
            user,
            required(body.recipientName(), "recipientName"),
            required(body.phone(), "phone"),
            required(body.zipCode(), "zipCode"),
            required(body.address1(), "address1"),
            trimToNull(body.address2()),
            isDefault
        );
        addressRepository.save(address);
        return toResponse(address);
    }

    public AddressResponseDto update(CurrentActor actor, long addressId, UpsertAddressDto body) {
        requireUser(actor);
        AddressEntity address = addressRepository.findByIdAndUserId(addressId, actor.id())
            .orElseThrow(() -> new AppException("ADDRESS_NOT_FOUND", "Address not found", HttpStatus.NOT_FOUND));
        boolean isDefault = body.isDefault() == null ? address.isDefault() : body.isDefault();
        if (isDefault) {
            addressRepository.clearDefaultByUserId(actor.id());
        }
        address.update(
            body.recipientName() == null ? address.getRecipientName() : required(body.recipientName(), "recipientName"),
            body.phone() == null ? address.getPhone() : required(body.phone(), "phone"),
            body.zipCode() == null ? address.getZipCode() : required(body.zipCode(), "zipCode"),
            body.address1() == null ? address.getAddress1() : required(body.address1(), "address1"),
            body.address2() == null ? address.getAddress2() : trimToNull(body.address2()),
            isDefault
        );
        addressRepository.save(address);
        return toResponse(address);
    }

    public MessageResponse remove(CurrentActor actor, long addressId) {
        requireUser(actor);
        AddressEntity address = addressRepository.findByIdAndUserId(addressId, actor.id())
            .orElseThrow(() -> new AppException("ADDRESS_NOT_FOUND", "Address not found", HttpStatus.NOT_FOUND));
        addressRepository.remove(address);
        return new MessageResponse("Address deleted successfully");
    }

    @Transactional(readOnly = true)
    public AddressEntity requireOwnedEntity(Long addressId, Long userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
            .orElseThrow(() -> new AppException("ADDRESS_NOT_FOUND", "Address not found", HttpStatus.NOT_FOUND));
    }

    private AddressResponseDto toResponse(AddressEntity address) {
        return new AddressResponseDto(
            address.getId(),
            address.getUser().getId(),
            address.getRecipientName(),
            address.getPhone(),
            address.getZipCode(),
            address.getAddress1(),
            address.getAddress2(),
            address.isDefault(),
            address.getCreatedAt(),
            address.getUpdatedAt()
        );
    }

    private String required(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new AppException("VALIDATION_ERROR", key + " is required", HttpStatus.BAD_REQUEST);
        }
        return value.trim();
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
}
