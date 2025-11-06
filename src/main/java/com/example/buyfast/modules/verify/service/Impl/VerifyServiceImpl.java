package com.example.buyfast.modules.verify.service.Impl;

import com.example.buyfast.modules.storage.service.StorageService; // <-- NEW IMPORT
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
// No longer needs VerificationRequest
import com.example.buyfast.modules.verify.model.Verify;
import com.example.buyfast.modules.verify.repository.VerifyRepo;
import com.example.buyfast.modules.verify.service.VerifyService;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // <-- NEW IMPORT

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyService {

    private final VerifyRepo verifyRepo;
    private final UserRepo userRepo;
    private final UuidService uuidService;
    private final StorageService storageService; // <-- NEW INJECTION

    @Override // <-- This now correctly overrides the interface
    @Transactional
    public void requestUserVerification(MultipartFile file, UserDetails userDetails) {
        User currentUser = (User) userDetails;

        // 1. Upload the file (this now calls PinataStorageServiceImpl)
        String fileUrl = storageService.uploadFile(file);

        // 2. Simple JSON string to store the document URL
        String documentsJson = "{\"id_card_url\": \"" + fileUrl + "\"}";

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

        User targetUser = userRepo.findByUuid(verification.getTargetId())
                .orElseThrow(() -> new IllegalStateException("Target user not found."));
        userRepo.setVerifiedStatus(targetUser.getId(), true);

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

        User targetUser = userRepo.findByUuid(verification.getTargetId())
                .orElseThrow(() -> new IllegalStateException("Target user not found."));
        userRepo.setVerifiedStatus(targetUser.getId(), false);

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