package com.example.buyfast.modules.address.service;

import com.example.buyfast.modules.address.dto.AddressRequest;
import com.example.buyfast.modules.address.model.ShippingAddress;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    ShippingAddress addAddress(@Valid AddressRequest request, UserDetails userDetails);

    List<ShippingAddress> getMyAddresses(UserDetails userDetails);

    void deleteAddress(UUID addressUuid, UserDetails userDetails);

    ShippingAddress updateAddress(UUID addressUuid, @Valid AddressRequest request, UserDetails userDetails);
}
