package com.example.buyfast.modules.otp.service.Impl;

import com.example.buyfast.modules.otp.service.SmsService;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    /**
     * DUMMY IMPLEMENTATION.
     * Replace this with a real SMS provider (Twilio, Vonage, etc.)
     */
    @Override
    public void sendSms(String to, String body) {
        System.out.println("---- SENDING SMS (DUMMY) ----");
        System.out.println("TO: " + to);
        System.out.println("BODY: " + body);
        System.out.println("-----------------------------");
        // In a real app:
        // twilioClient.messages.create(
        //      new PhoneNumber(to),
        //      new PhoneNumber(yourTwilioNumber),
        //      body
        // );
    }
}