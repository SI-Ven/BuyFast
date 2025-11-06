package com.example.buyfast.modules.otp.repository;

import com.example.buyfast.modules.otp.model.Otp;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface OtpRepo {

    /**
     * Inserts or updates an OTP for a given email.
     * ON CONFLICT(email) ensures that if an OTP already exists, it's updated.
     */
    @Insert("INSERT INTO otp_number (email, otp_code, expires_at) " +
            "VALUES (#{email}, #{otpCode}, #{expiresAt}) " +
            "ON CONFLICT (email) DO UPDATE SET " +
            "otp_code = EXCLUDED.otp_code, " +
            "expires_at = EXCLUDED.expires_at")
    void save(Otp otp);

    @Select("SELECT * FROM otp_number WHERE email = #{email} AND otp_code = #{otpCode}")
    Optional<Otp> findByEmailAndOtpCode(String email, String otpCode);

    @Delete("DELETE FROM otp_number WHERE email = #{email}")
    void deleteByEmail(String email);
}