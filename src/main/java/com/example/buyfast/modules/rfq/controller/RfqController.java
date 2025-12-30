package com.example.buyfast.modules.rfq.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.rfq.service.RfqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/api/rfq")
@RequiredArgsConstructor
public class RfqController {
    private final RfqService rfqService;
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<String>> submitRfq(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String productName,
            @RequestParam int quantity,
            @RequestParam BigDecimal targetPrice) {

        rfqService.submitRfq(Long.valueOf(userDetails.getUsername()), productName, quantity, targetPrice);
        return ResponseEntity.ok(ApiResponse.success("Rfq submitted successfully.", null));
    }

}
