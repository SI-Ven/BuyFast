package com.example.buyfast.modules.verify.service.Impl;

import com.example.buyfast.modules.storage.service.StorageService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.modules.verify.model.Verify;
import com.example.buyfast.modules.verify.repository.VerifyRepo;
import com.example.buyfast.modules.verify.service.VerifyService;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyService {

    private final VerifyRepo verifyRepo;
    private final UserRepo userRepo;
    private final UuidService uuidService;
    private final StorageService storageService;

    // --- MODIFIED ---
    @Override
    @Transactional
    public Verify requestUserVerification(MultipartFile file, UserDetails userDetails) {
        User currentUser = (User) userDetails;

        String fileUrl = storageService.uploadFile(file);
        String documentsJson = "{\"id_card_url\": \"" + fileUrl + "\"}";

        Verify verification = new Verify();
        verification.setVerifyUuid(uuidService.generateUuid());
        verification.setTargetType("user");
        verification.setTargetId(currentUser.getUserUuid());
        verification.setSubmittedBy(currentUser.getId());
        verification.setVerifyDocuments(documentsJson);
        // Status defaults to 'pending' in the DB

        verifyRepo.createVerification(verification);
        return verification; // <-- RETURN VERIFICATION
    }

    // --- MODIFIED ---
    @Override
    @Transactional
    public Verify approveVerification(UUID verifyUuid, String remarks, UserDetails adminDetails) {
        User adminUser = (User) adminDetails;
        Verify verification = findVerification(verifyUuid);

        User targetUser = userRepo.findByUuid(verification.getTargetId())
                .orElseThrow(() -> new IllegalStateException("Target user not found."));
        userRepo.setVerifiedStatus(targetUser.getId(), true);

        verification.setStatus("approved");
        verification.setRemarks(remarks);
        verification.setReviewedBy(adminUser.getId());
        verifyRepo.updateVerificationStatus(verification);

        return verification; // <-- RETURN VERIFICATION
    }

    // --- MODIFIED ---
    @Override
    @Transactional
    public Verify rejectVerification(UUID verifyUuid, String remarks, UserDetails adminDetails) {
        User adminUser = (User) adminDetails;
        Verify verification = findVerification(verifyUuid);

        User targetUser = userRepo.findByUuid(verification.getTargetId())
                .orElseThrow(() -> new IllegalStateException("Target user not found."));
        userRepo.setVerifiedStatus(targetUser.getId(), false);

        verification.setStatus("rejected");
        verification.setRemarks(remarks);
        verification.setReviewedBy(adminUser.getId());
        verifyRepo.updateVerificationStatus(verification);

        return verification; // <-- RETURN VERIFICATION
    }

    private Verify findVerification(UUID verifyUuid) {
        return verifyRepo.findByUuid(verifyUuid)
                .orElseThrow(() -> new IllegalStateException("Verification request not found."));
    }
}