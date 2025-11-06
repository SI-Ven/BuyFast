package com.example.buyfast.modules.otp.service;

public interface SmsService {
    /**
     * Sends an SMS message.
     * @param to The phone number to send to (e.g., "+1234567890")
     * @param body The text message
     */
    void sendSms(String to, String body);
}