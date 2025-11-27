package com.example.buyfast.modules.address.service.impl;

import com.example.buyfast.modules.address.dto.AddressRequest;
import com.example.buyfast.modules.address.model.ShippingAddress;
import com.example.buyfast.modules.address.repository.AddressRepo;
import com.example.buyfast.modules.address.service.AddressService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepo addressRepo;
    private final UserRepo userRepo;

    @Override
    @Transactional
    public ShippingAddress addAddress(AddressRequest request, UserDetails userDetails) {
        User user = getUser(userDetails);
        // If this new address is default, unset previous defaults
        if (request.isDefault()) {
            addressRepo.removeAllDefaultsForUser(user.getId());
        }
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .addressUuid(UUID.randomUUID())
                .userId(user.getId())
                .fullName(request.getFullName())
                .addressLine1(request.getAddressLine1())
                .city(request.getCity())
                .country(request.getCountry())
                .isDefault(request.isDefault())
                .build();

        return addressRepo.save(shippingAddress);
    }

    @Override
    public List<ShippingAddress> getMyAddresses(UserDetails userDetails) {
        User user = getUser(userDetails);
        return addressRepo.findAllByUserId(user.getId());
    }

    @Override
    @Transactional
    public void deleteAddress(UUID addressUuid, UserDetails userDetails) {
        User user = getUser(userDetails);
        addressRepo.deleteByUuidAndUser(addressUuid, user.getId());
    }

    @Override
    @Transactional
    public ShippingAddress updateAddress(UUID addressUuid, AddressRequest request, UserDetails userDetails) {
        User user = getUser(userDetails);

        // 1. Check if the address exists and belongs to the user
        ShippingAddress existingAddress = addressRepo.findByUuidAndUser(addressUuid, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        // 2. If setting as default, unset others
        if (request.isDefault()) {
            addressRepo.removeAllDefaultsForUser(user.getId());
        }

        // 3. Update fields
        existingAddress.setFullName(request.getFullName());
        existingAddress.setAddressLine1(request.getAddressLine1());
        existingAddress.setCity(request.getCity());
        existingAddress.setCountry(request.getCountry());
        existingAddress.setIsDefault(request.isDefault());

        // 4. Save updates
        addressRepo.update(existingAddress);

        return existingAddress;
    }

    private User getUser(UserDetails userDetails) {
        return userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
