package com.example.buyfast.modules.verify.service;

import com.example.buyfast.modules.verify.dto.VerificationRequest;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.UUID;

public interface VerifyService {

    /**
     * A user submits a request to verify their identity.
     */
    void requestUserVerification(VerificationRequest request, UserDetails userDetails);

    /**
     * An admin approves a verification request.
     */
    void approveVerification(UUID verifyUuid, String remarks, UserDetails adminDetails);

    /**
     * An admin rejects a verification request.
     */
    void rejectVerification(UUID verifyUuid, String remarks, UserDetails adminDetails);
}