package com.example.buyfast.modules.otp.service.impl; // <-- New package

import com.example.buyfast.modules.otp.service.EmailService; // <-- Import interface
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService { // <-- Implements interface

    private final JavaMailSender mailSender;

    @Override // <-- Add annotation
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@buyfast.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}