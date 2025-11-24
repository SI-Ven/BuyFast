package com.example.buyfast.modules.notification;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
