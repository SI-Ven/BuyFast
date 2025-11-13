package com.example.buyfast.modules.otp.service.Impl;

import com.example.buyfast.modules.otp.model.Otp;
import com.example.buyfast.modules.otp.repository.OtpRepo;
import com.example.buyfast.modules.otp.service.EmailService;
import com.example.buyfast.modules.otp.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepo otpRepo;
    private final EmailService emailService;

    private String generateOtpCode() {
        return String.format("%06d", new Random().nextInt(900000) + 100000);
    }

    @Override
    @Transactional
    public void sendOtp(String email) {
        String otpCode = generateOtpCode();
        System.out.println("GENERATING OTP for " + email + ": " + otpCode); // --- DEBUG LOG ---

        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        otpRepo.save(otp);

        String subject = "Your BuyFast Verification Code";
        String body = "Your verification code is: " + otpCode + "\n" +
                "It will expire in 5 minutes.";

        try {
            emailService.sendSimpleMessage(email, subject, body);
            System.out.println("OTP Email sent successfully to " + email);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email to " + email + ": " + e.getMessage());
            // We do NOT throw exception here, so the DB save persists even if email fails
        }
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        System.out.println("VERIFYING OTP for " + email + " with code " + otpCode); // --- DEBUG LOG ---

        Optional<Otp> otpOpt = otpRepo.findByEmailAndOtpCode(email, otpCode);

        if (otpOpt.isEmpty()) {
            System.out.println("OTP Verification Failed: No matching record found.");
            return false;
        }

        Otp otp = otpOpt.get();

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            System.out.println("OTP Verification Failed: OTP expired at " + otp.getExpiresAt());
            otpRepo.deleteByEmail(email);
            return false;
        }

        // OTP is valid
        System.out.println("OTP Verification Success!");
        otpRepo.deleteByEmail(email); // Consume the OTP so it can't be used again
        return true;
    }
}