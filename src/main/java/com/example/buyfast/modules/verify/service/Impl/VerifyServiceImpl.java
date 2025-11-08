package com.example.buyfast.modules.verify.service.Impl;

import com.example.buyfast.modules.company.model.Company; // <-- NEW IMPORT
import com.example.buyfast.modules.company.repository.CompanyRepo; // <-- NEW IMPORT
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
    private final CompanyRepo companyRepo; // <-- NEW: Inject CompanyRepo

    @Override
    @Transactional
    public Verify requestUserVerification(MultipartFile file, UserDetails userDetails) {
        User currentUser = (User) userDetails;

        // Check for existing pending request
        verifyRepo.findPendingRequest(currentUser.getUserUuid(), "user").ifPresent(v -> {
            throw new IllegalStateException("You already have a pending ID verification request.");
        });

        String fileUrl = storageService.uploadFile(file);
        String documentsJson = "{\"id_card_url\": \"" + fileUrl + "\"}";

        Verify verification = new Verify();
        verification.setVerifyUuid(uuidService.generateUuid());
        verification.setTargetType("user"); // <-- This is for user ID card
        verification.setTargetId(currentUser.getUserUuid());
        verification.setSubmittedBy(currentUser.getId());
        verification.setVerifyDocuments(documentsJson);
        verification.setStatus("pending");

        verifyRepo.createVerification(verification);
        return verification;
    }

    // --- MODIFIED ---
    @Override
    @Transactional
    public Verify approveVerification(UUID verifyUuid, String remarks, UserDetails adminDetails) {
        User adminUser = (User) adminDetails;
        Verify verification = findAndCheckVerification(verifyUuid);

        // Use a switch to handle different verification types
        switch (verification.getTargetType()) {
            case "user":
                User userToVerify = findUserByUuid(verification.getTargetId());
                userRepo.setVerifiedStatus(userToVerify.getId(), true);
                break;
            case "seller":
                User userToPromote = findUserByUuid(verification.getTargetId());
                userRepo.updateUserRole(userToPromote.getId(), "seller");
                break;
            case "company":
                Company companyToApprove = findCompanyByUuid(verification.getTargetId());
                companyRepo.updateCompanyStatus(companyToApprove.getId(), "active");
                break;
            default:
                throw new IllegalStateException("Unknown verification target type: " + verification.getTargetType());
        }

        // Mark verification as approved
        verification.setStatus("approved");
        verification.setRemarks(remarks);
        verification.setReviewedBy(adminUser.getId());
        verifyRepo.updateVerificationStatus(verification);

        return verification;
    }

    // --- MODIFIED ---
    @Override
    @Transactional
    public Verify rejectVerification(UUID verifyUuid, String remarks, UserDetails adminDetails) {
        User adminUser = (User) adminDetails;
        Verify verification = findAndCheckVerification(verifyUuid);

        // Update target based on type
        switch (verification.getTargetType()) {
            case "user":
                User userToVerify = findUserByUuid(verification.getTargetId());
                userRepo.setVerifiedStatus(userToVerify.getId(), false);
                break;
            case "seller":
                // No change to user role, just reject the request
                break;
            case "company":
                Company companyToReject = findCompanyByUuid(verification.getTargetId());
                companyRepo.updateCompanyStatus(companyToReject.getId(), "rejected");
                break;
            default:
                throw new IllegalStateException("Unknown verification target type: " + verification.getTargetType());
        }

        // Mark verification as rejected
        verification.setStatus("rejected");
        verification.setRemarks(remarks);
        verification.setReviewedBy(adminUser.getId());
        verifyRepo.updateVerificationStatus(verification);

        return verification;
    }

    // --- HELPER METHODS ---

    private Verify findAndCheckVerification(UUID verifyUuid) {
        Verify verification = verifyRepo.findByUuid(verifyUuid)
                .orElseThrow(() -> new IllegalStateException("Verification request not found."));
        if (!"pending".equals(verification.getStatus())) {
            throw new IllegalStateException("This request has already been " + verification.getStatus());
        }
        return verification;
    }

    private User findUserByUuid(UUID userUuid) {
        return userRepo.findByUuid(userUuid)
                .orElseThrow(() -> new IllegalStateException("Target user not found for UUID: " + userUuid));
    }

    private Company findCompanyByUuid(UUID companyUuid) {
        return companyRepo.findByUuid(companyUuid)
                .orElseThrow(() -> new IllegalStateException("Target company not found for UUID: " + companyUuid));
    }
}