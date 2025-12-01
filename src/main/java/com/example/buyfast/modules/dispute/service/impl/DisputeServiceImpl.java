package com.example.buyfast.modules.dispute.service.impl;

import com.example.buyfast.modules.dispute.model.Dispute;
import com.example.buyfast.modules.dispute.repository.DisputeRepo;
import com.example.buyfast.modules.dispute.service.DisputeService;
import com.example.buyfast.modules.order.model.Order;
import com.example.buyfast.modules.order.repository.OrderRepo;
import com.example.buyfast.modules.storage.service.StorageService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {
    private final UserRepo userRepo;
    private final OrderRepo orderRepo;
    private final DisputeRepo disputeRepo;
    private final StorageService storageService;


    @Override
    @Transactional
    public void createDispute(String username, UUID orderUuid, String reason, String description, List<MultipartFile> evidence) {
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepo.findByUuid(orderUuid)
                .orElseThrow(()->new RuntimeException("Order not found"));

//  Security Check: Ensure user owns the order
        if(!order.getUserId().equals(user.getId())){
            throw new SecurityException("Access denied: You can only raise disputes for your own orders.");
        }

        Dispute dispute = new Dispute();
        dispute.setOrderId(order.getId());
        dispute.setUserId(user.getId());
        dispute.setReason(reason);
        dispute.setDescription(description);
        dispute.setStatus("OPEN");
        dispute.setCreatedAt(LocalDateTime.now());

        // save into db
        disputeRepo.save(dispute);

        if (evidence != null && !evidence.isEmpty()) {
            for (MultipartFile multipartFile : evidence) {
                if (!multipartFile.isEmpty()) {
                    String ImageUrl = storageService.uploadFile(multipartFile);

                    disputeRepo.saveEvidence(dispute.getId(), ImageUrl);
                }
            }
        }
    }

    @Override
    @Transactional
    public void resolveDispute(Long id, String status, String adminComment) {
        // Validate status
        if (!status.equals("RESOLVED") && !status.equals("REJECTED")) {
            throw new IllegalArgumentException("Invalid status. Must be RESOLVED or REJECTED.");
        }

        // Check if dispute exists
        disputeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found with ID: " + id));

        // Update
        disputeRepo.updateStatus(id, status, adminComment);
    }
}
