package com.example.buyfast.modules.dispute.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DisputeService {
    void createDispute(String username, UUID orderUuid, String reason, String description, List<MultipartFile> evidence);

    void resolveDispute(Long id, String status, String adminComment);
}
