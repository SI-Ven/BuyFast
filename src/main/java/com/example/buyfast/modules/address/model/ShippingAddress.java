package com.example.buyfast.modules.address.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ShippingAddress {
    private Long id;
    private UUID addressUuid;
    private Long userId;
    private String fullName;
    private String addressLine1;  //No. 123, Street 2004
    private String city;
    private String country;
    private Boolean isDefault;
}
