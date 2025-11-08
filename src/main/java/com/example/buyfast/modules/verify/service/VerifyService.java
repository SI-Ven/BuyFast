package com.example.buyfast.modules.verify.service;

import com.example.buyfast.modules.verify.model.Verify; // <-- NEW IMPORT
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface VerifyService {

    /**
     * A user submits a request to verify their identity by uploading a file.
     */
    // --- MODIFIED ---
    Verify requestUserVerification(MultipartFile file, UserDetails userDetails);

    /**
     * An admin approves a verification request.
     */
    // --- MODIFIED ---
    Verify approveVerification(UUID verifyUuid, String remarks, UserDetails adminDetails);

    /**
     * An admin rejects a verification request.
     */
    // --- MODIFIED ---
    Verify rejectVerification(UUID verifyUuid, String remarks, UserDetails adminDetails);
}