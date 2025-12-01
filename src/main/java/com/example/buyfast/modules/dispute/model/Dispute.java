package com.example.buyfast.modules.dispute.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Dispute {
    private Long id;
    private Long orderId;
    private Long userId;
    private String reason;
    private String description;
    private String status;
    private String adminComment;
    private List<String> evidenceImages; // List of URLs
    private LocalDateTime createdAt;
}