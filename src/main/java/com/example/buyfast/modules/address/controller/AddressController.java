package com.example.buyfast.modules.address.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.address.dto.AddressRequest;
import com.example.buyfast.modules.address.model.ShippingAddress;
import com.example.buyfast.modules.address.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.Address;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shipping-address")
public class AddressController {
    private final AddressService addressService;
    @PostMapping
    @Operation(summary = "Add a new shipping address")
    public ResponseEntity<ApiResponse<ShippingAddress>> addAddress(@Valid @RequestBody AddressRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        ShippingAddress shippingAddress1 = addressService.addAddress(request,userDetails);
        return ResponseEntity.ok(ApiResponse.success("add address successfully",shippingAddress1));

    }

    @GetMapping
    @Operation(summary = "Get all my shipping addresses")
    public ResponseEntity<ApiResponse<List<ShippingAddress>>> getMyAddresses(@AuthenticationPrincipal UserDetails userDetails) {
        List<ShippingAddress> addresses = addressService.getMyAddresses(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved.", addresses));
    }
    @DeleteMapping("/{addressUuid}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable UUID addressUuid,
            @AuthenticationPrincipal UserDetails userDetails) {

        addressService.deleteAddress(addressUuid, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully.", null));
    }

    @PutMapping("/{addressUuid}")
    @Operation(summary = "Update an existing address")
    public ResponseEntity<ApiResponse<ShippingAddress>> updateAddress(
            @PathVariable UUID addressUuid,
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ShippingAddress updatedAddress = addressService.updateAddress(addressUuid, request, userDetails);

        return ResponseEntity.ok(ApiResponse.success("Address updated successfully.", updatedAddress));
    }
}
