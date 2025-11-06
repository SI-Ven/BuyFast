package com.example.buyfast.modules.verify.service.Impl;

import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.modules.verify.dto.VerificationRequest;
import com.example.buyfast.modules.verify.model.Verify;
import com.example.buyfast.modules.verify.repository.VerifyRepo;
import com.example.buyfast.modules.verify.service.VerifyService;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyService {

    private final VerifyRepo verifyRepo;
    private final UserRepo userRepo;
    private final UuidService uuidService;

    @Override
    @Transactional
    public void requestUserVerification(VerificationRequest request, UserDetails userDetails) {
        User currentUser = (User) userDetails;

        // Simple JSON string to store the document URL
        String documentsJson = "{\"id_card_url\": \"" + request.getIdCardUrl() + "\"}";

        Verify verification = new Verify();
        verification.setVerifyUuid(uuidService.generateUuid());
        verification.setTargetType("user");
        verification.setTargetId(currentUser.getUserUuid()); // The public UUID of the user
        verification.setSubmittedBy(currentUser.getId());  // The internal ID of the user
        verification.setVerifyDocuments(documentsJson);

        verifyRepo.createVerification(verification);
    }

    @Override
    @Transactional
    public void approveVerification(UUID verifyUuid, String remarks, UserDetails adminDetails) {
        User adminUser = (User) adminDetails;
        Verify verification = findVerification(verifyUuid);

        // Set user's 'verified' status to true
        User targetUser = userRepo.findByUuid(verification.getTargetId())
                .orElseThrow(() -> new IllegalStateException("Target user not found."));
        userRepo.setVerifiedStatus(targetUser.getId(), true);

        // Update verification record
        verification.setStatus("approved");
        verification.setRemarks(remarks);
        verification.setReviewedBy(adminUser.getId());
        verifyRepo.updateVerificationStatus(verification);
    }

    @Override
    @Transactional
    public void rejectVerification(UUID verifyUuid, String remarks, UserDetails adminDetails) {
        User adminUser = (User) adminDetails;
        Verify verification = findVerification(verifyUuid);

        // Set user's 'verified' status to false
        User targetUser = userRepo.findByUuid(verification.getTargetId())
                .orElseThrow(() -> new IllegalStateException("Target user not found."));
        userRepo.setVerifiedStatus(targetUser.getId(), false);

        // Update verification record
        verification.setStatus("rejected");
        verification.setRemarks(remarks);
        verification.setReviewedBy(adminUser.getId());
        verifyRepo.updateVerificationStatus(verification);
    }

    private Verify findVerification(UUID verifyUuid) {
        return verifyRepo.findByUuid(verifyUuid)
                .orElseThrow(() -> new IllegalStateException("Verification request not found."));
    }
}