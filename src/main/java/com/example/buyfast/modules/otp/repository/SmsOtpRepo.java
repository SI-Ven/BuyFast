package com.example.buyfast.modules.otp.repository;

import com.example.buyfast.modules.otp.model.SmsOtp;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface SmsOtpRepo {

    @Insert("INSERT INTO sms_otp (phone_number, otp_code, expires_at) " +
            "VALUES (#{phoneNumber}, #{otpCode}, #{expiresAt}) " +
            "ON CONFLICT (phone_number) DO UPDATE SET " +
            "otp_code = EXCLUDED.otp_code, " +
            "expires_at = EXCLUDED.expires_at")
    void save(SmsOtp otp);

    @Select("SELECT * FROM sms_otp WHERE phone_number = #{phoneNumber} AND otp_code = #{otpCode}")
    Optional<SmsOtp> findByPhoneAndOtpCode(String phoneNumber, String otpCode);

    @Delete("DELETE FROM sms_otp WHERE phone_number = #{phoneNumber}")
    void deleteByPhone(String phoneNumber);
}