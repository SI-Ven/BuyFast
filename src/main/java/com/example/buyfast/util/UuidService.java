package com.example.buyfast.util;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class UuidService {

    /**
     * Generates a new, random (Type 4) UUID.
     * @return a UUID
     */
    public UUID generateUuid() {
        return UUID.randomUUID();
    }
}