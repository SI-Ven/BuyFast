package com.example.buyfast.modules.verify.service;

// No longer needs VerificationRequest
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile; // <-- NEW IMPORT
import java.util.UUID;

public interface VerifyService {

    /**
     * A user submits a request to verify their identity by uploading a file.
     */
    void requestUserVerification(MultipartFile file, UserDetails userDetails); // <-- MODIFIED

    /**
     * An admin approves a verification request.
     */
    void approveVerification(UUID verifyUuid, String remarks, UserDetails adminDetails);

    /**
     * An admin rejects a verification request.
     */
    void rejectVerification(UUID verifyUuid, String remarks, UserDetails adminDetails);
}