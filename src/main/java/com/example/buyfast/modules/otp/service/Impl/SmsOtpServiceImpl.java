package com.example.buyfast.modules.otp.service.Impl;

import com.example.buyfast.modules.otp.model.SmsOtp;
import com.example.buyfast.modules.otp.repository.SmsOtpRepo;
import com.example.buyfast.modules.otp.service.SmsOtpService;
import com.example.buyfast.modules.otp.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SmsOtpServiceImpl implements SmsOtpService {

    private final SmsOtpRepo smsOtpRepo;
    private final SmsService smsService; // Injects the dummy SMS sender

    private String generateOtpCode() {
        return String.format("%06d", new Random().nextInt(900000) + 100000);
    }

    @Override
    @Transactional
    public void sendOtp(String phoneNumber) {
        String otpCode = generateOtpCode();
        SmsOtp otp = new SmsOtp();
        otp.setPhoneNumber(phoneNumber);
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        smsOtpRepo.save(otp);

        String body = "Your BuyFast verification code is: " + otpCode;

        try {
            smsService.sendSms(phoneNumber, body);
        } catch (Exception e) {
            System.err.println("Failed to send SMS OTP to " + phoneNumber + ": " + e.getMessage());
            // We catch this so the transaction doesn't roll back
        }
    }

    @Override
    @Transactional
    public boolean verifyOtp(String phoneNumber, String otpCode) {
        Optional<SmsOtp> otpOpt = smsOtpRepo.findByPhoneAndOtpCode(phoneNumber, otpCode);

        if (otpOpt.isEmpty()) {
            return false;
        }

        SmsOtp otp = otpOpt.get();

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            smsOtpRepo.deleteByPhone(phoneNumber);
            return false;
        }

        smsOtpRepo.deleteByPhone(phoneNumber);
        return true;
    }
}