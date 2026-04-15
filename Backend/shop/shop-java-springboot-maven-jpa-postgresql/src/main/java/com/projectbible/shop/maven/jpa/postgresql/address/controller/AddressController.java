package com.projectbible.shop.maven.jpa.postgresql.address.presentation;

import com.projectbible.shop.maven.jpa.postgresql.address.application.AddressService;
import com.projectbible.shop.maven.jpa.postgresql.common.api.ApiResponse;
import com.projectbible.shop.maven.jpa.postgresql.common.security.AuthContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
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
    public ApiResponse<List<Map<String, Object>>> list(HttpServletRequest request) {
        return ApiResponse.success(service.list(AuthContext.requireUser(request)));
    }

    @Operation(summary = "Create address")
    @PostMapping
    public ApiResponse<Map<String, Object>> create(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(service.create(AuthContext.requireUser(request), body));
    }

    @Operation(summary = "Update address")
    @PatchMapping("/{addressId}")
    public ApiResponse<Map<String, Object>> update(HttpServletRequest request, @PathVariable long addressId, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(service.update(AuthContext.requireUser(request), addressId, body));
    }

    @Operation(summary = "Delete address")
    @DeleteMapping("/{addressId}")
    public ApiResponse<Map<String, Object>> remove(HttpServletRequest request, @PathVariable long addressId) {
        return ApiResponse.success(service.remove(AuthContext.requireUser(request), addressId));
    }
}
