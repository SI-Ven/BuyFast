package com.example.buyfast.modules.verify.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.verify.model.Verify; // <-- NEW IMPORT
import com.example.buyfast.modules.verify.service.VerifyService;
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

    // --- MODIFIED ---
    @PostMapping(value = "/request-user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Verify>> requestUserVerification( // <-- Changed to ApiResponse<Verify>
                                                                        @RequestPart("file") MultipartFile file,
                                                                        @AuthenticationPrincipal UserDetails userDetails) {

        Verify verification = verifyService.requestUserVerification(file, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Verification request submitted successfully.", verification)); // <-- Add payload
    }

    // --- MODIFIED ---
    @PostMapping("/approve/{verifyUuid}")
    public ResponseEntity<ApiResponse<Verify>> approveRequest( // <-- Changed to ApiResponse<Verify>
                                                               @PathVariable UUID verifyUuid,
                                                               @RequestBody(required = false) Map<String, String> payload,
                                                               @AuthenticationPrincipal UserDetails adminDetails) {

        String remarks = (payload != null) ? payload.get("remarks") : "Approved";
        Verify verification = verifyService.approveVerification(verifyUuid, remarks, adminDetails);
        return ResponseEntity.ok(ApiResponse.success("Verification approved.", verification)); // <-- Add payload
    }

    // --- MODIFIED ---
    @PostMapping("/reject/{verifyUuid}")
    public ResponseEntity<ApiResponse<Verify>> rejectRequest( // <-- Changed to ApiResponse<Verify>
                                                              @PathVariable UUID verifyUuid,
                                                              @RequestBody Map<String, String> payload,
                                                              @AuthenticationPrincipal UserDetails adminDetails) {

        String remarks = payload.get("remarks");
        if (remarks == null || remarks.isBlank()) {
            throw new IllegalStateException("Rejection remarks are required.");
        }

        Verify verification = verifyService.rejectVerification(verifyUuid, remarks, adminDetails);
        return ResponseEntity.ok(ApiResponse.success("Verification rejected.", verification)); // <-- Add payload
    }
}