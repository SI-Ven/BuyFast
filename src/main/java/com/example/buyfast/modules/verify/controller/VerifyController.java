package com.example.buyfast.modules.verify.controller;

import com.example.buyfast.modules.verify.dto.VerificationRequest;
import com.example.buyfast.modules.verify.service.VerifyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/verify")
@RequiredArgsConstructor
public class VerifyController {

    private final VerifyService verifyService;

    /**
     * Endpoint for a user to submit their ID card for verification.
     */
    @PostMapping(value = "/request-user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // <-- MODIFIED
    public ResponseEntity<Map<String, String>> requestUserVerification(
            @RequestPart("file") MultipartFile file, // <-- MODIFIED
            @AuthenticationPrincipal UserDetails userDetails) {

        verifyService.requestUserVerification(file, userDetails); // <-- MODIFIED
        return ResponseEntity.ok(Map.of("message", "Verification request submitted successfully."));
    }

    /**
     * Endpoint for an ADMIN to approve a request.
     */
    @PostMapping("/approve/{verifyUuid}")
    public ResponseEntity<Map<String, String>> approveRequest(
            @PathVariable UUID verifyUuid,
            @RequestBody(required = false) Map<String, String> payload,
            @AuthenticationPrincipal UserDetails adminDetails) {

        String remarks = (payload != null) ? payload.get("remarks") : "Approved";
        verifyService.approveVerification(verifyUuid, remarks, adminDetails);
        return ResponseEntity.ok(Map.of("message", "Verification approved."));
    }

    /**
     * Endpoint for an ADMIN to reject a request.
     */
    @PostMapping("/reject/{verifyUuid}")
    public ResponseEntity<Map<String, String>> rejectRequest(
            @PathVariable UUID verifyUuid,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails adminDetails) {

        String remarks = payload.get("remarks");
        if (remarks == null || remarks.isBlank()) {
            throw new IllegalStateException("Rejection remarks are required.");
        }

        verifyService.rejectVerification(verifyUuid, remarks, adminDetails);
        return ResponseEntity.ok(Map.of("message", "Verification rejected."));
    }
}