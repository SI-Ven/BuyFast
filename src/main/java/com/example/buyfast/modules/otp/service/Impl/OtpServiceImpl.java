package com.example.buyfast.modules.otp.service.Impl;

import com.example.buyfast.modules.otp.model.Otp;
import com.example.buyfast.modules.otp.repository.OtpRepo;
import com.example.buyfast.modules.otp.service.EmailService;
import com.example.buyfast.modules.otp.service.OtpService; // <-- Import interface
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService { // <-- Implements interface

    private final OtpRepo otpRepo;
    private final EmailService emailService; // <-- Injects EmailService interface

    private String generateOtpCode() {
        return String.format("%06d", new Random().nextInt(900000) + 100000);
    }

    @Override // <-- Add annotation
    @Transactional
    public void sendOtp(String email) {
        String otpCode = generateOtpCode();
        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        otpRepo.save(otp);

        String subject = "Your BuyFast Verification Code";
        String body = "Your verification code is: " + otpCode + "\n" +
                "It will expire in 5 minutes.";

        emailService.sendSimpleMessage(email, subject, body);
    }

    @Override // <-- Add annotation
    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        Optional<Otp> otpOpt = otpRepo.findByEmailAndOtpCode(email, otpCode);

        if (otpOpt.isEmpty()) {
            return false;
        }

        Otp otp = otpOpt.get();

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        otpRepo.deleteByEmail(email);
        return true;
    }
}