package com.projectbible.shop.address.controller;

import com.projectbible.shop.address.dto.AddressDtos.AddressResponseDto;
import com.projectbible.shop.address.dto.AddressDtos.UpsertAddressDto;
import com.projectbible.shop.address.service.AddressService;
import com.projectbible.shop.common.api.ApiResponse;
import com.projectbible.shop.common.api.MessageResponse;
import com.projectbible.shop.common.security.AuthContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "addresses")
@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {
    private final AddressService service;

    public AddressController(AddressService service) {
        this.service = service;
    }

    @Operation(summary = "List addresses")
    @GetMapping
    public ApiResponse<List<AddressResponseDto>> list(HttpServletRequest request) {
        return ApiResponse.success(service.list(AuthContext.requireUser(request)));
    }

    @Operation(summary = "Create address")
    @PostMapping
    public ApiResponse<AddressResponseDto> create(HttpServletRequest request, @RequestBody UpsertAddressDto body) {
        return ApiResponse.success(service.create(AuthContext.requireUser(request), body));
    }

    @Operation(summary = "Update address")
    @PatchMapping("/{addressId}")
    public ApiResponse<AddressResponseDto> update(HttpServletRequest request, @PathVariable long addressId, @RequestBody UpsertAddressDto body) {
        return ApiResponse.success(service.update(AuthContext.requireUser(request), addressId, body));
    }

    @Operation(summary = "Delete address")
    @DeleteMapping("/{addressId}")
    public ApiResponse<MessageResponse> remove(HttpServletRequest request, @PathVariable long addressId) {
        return ApiResponse.success(service.remove(AuthContext.requireUser(request), addressId));
    }
}
