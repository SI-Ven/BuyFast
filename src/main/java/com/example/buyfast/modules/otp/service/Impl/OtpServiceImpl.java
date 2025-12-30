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
        String normalizedEmail = email.toLowerCase().trim();
        String otpCode = generateOtpCode();

        System.out.println("--- SENDING OTP ---");
        System.out.println("Email: " + normalizedEmail);
        System.out.println("Code: " + otpCode);
        System.out.println("Time: " + LocalDateTime.now());

        Otp otp = new Otp();
        otp.setEmail(normalizedEmail);
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        otpRepo.save(otp);

        String subject = "Your BuyFast Verification Code";
        String body = "Your verification code is: " + otpCode + "\n" +
                "It will expire in 5 minutes.";

        try {
            emailService.sendSimpleMessage(normalizedEmail, subject, body);
            System.out.println("OTP Email sent successfully.");
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        String normalizedEmail = email.toLowerCase().trim();
        String normalizedOtp = otpCode.trim();

        System.out.println("--- VERIFYING OTP ---");
        System.out.println("Email: " + normalizedEmail);
        System.out.println("Input: " + normalizedOtp);

        Optional<Otp> otpOpt = otpRepo.findByEmailAndOtpCode(normalizedEmail, normalizedOtp);

        if (otpOpt.isEmpty()) {
            System.out.println("RESULT: OTP Not Found (Check DB insert or Case)");
            return false;
        }

        Otp otp = otpOpt.get();
        LocalDateTime now = LocalDateTime.now();

        if (otp.getExpiresAt().isBefore(now)) {
            System.out.println("RESULT: OTP Expired at " + otp.getExpiresAt());
            otpRepo.deleteByEmail(normalizedEmail);
            return false;
        }

        System.out.println("RESULT: OTP Valid");


        return true;
    }
}