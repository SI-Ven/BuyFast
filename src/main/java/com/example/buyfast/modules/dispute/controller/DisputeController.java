package com.example.buyfast.modules.dispute.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.dispute.service.DisputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> createDispute(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("orderUuid") UUID orderUuid,
            @RequestParam("reason") String reason,
            @RequestParam("description") String description,
            @RequestParam(value = "evidence", required = false) List<MultipartFile> evidence
    ) {
        disputeService.createDispute(userDetails.getUsername(), orderUuid, reason, description, evidence);
        return ResponseEntity.ok(ApiResponse.success("Dispute created"));
    }

    // Admin Endpoint
    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<String>> resolveDispute(
            @PathVariable Long id,
            @RequestParam String status, // 'RESOLVED' or 'REJECTED'
            @RequestParam String adminComment
    ) {
        disputeService.resolveDispute(id, status, adminComment);
        return ResponseEntity.ok(ApiResponse.success("Dispute resolved"));
    }
}