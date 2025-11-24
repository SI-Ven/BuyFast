package com.example.buyfast.modules.auth.repository;

import com.example.buyfast.modules.auth.model.Otp;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface OtpRepository {

    // 1. INSERT (No @Results here!)
    @Insert("INSERT INTO auth.otps (email, otp_code, is_used, created_at, expires_at) " +
            "VALUES (#{email}, #{otpCode}, #{isUsed}, #{createdAt}, #{expiresAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Otp otp);

    // 2. SELECT - DEFINE THE MAP HERE
    @Select("SELECT * FROM auth.otps " +
            "WHERE otp_code = #{otpCode} AND email = #{email} " +
            "AND is_used = false AND expires_at > CURRENT_TIMESTAMP")
    @Results(id = "otpResultMap", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "email", column = "email"),
            @Result(property = "otpCode", column = "otp_code"),
            @Result(property = "isUsed", column = "is_used"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "expiresAt", column = "expires_at")
    })
    Optional<Otp> findByEmailAndOtpCode(@Param("email") String email, @Param("otpCode") String otpCode);

    // 3. SELECT BY ID - REUSE THE MAP
    @Select("SELECT * FROM auth.otps WHERE id = #{id}")
    @ResultMap("otpResultMap") // Now this works because findByEmailAndOtpCode defined it
    Optional<Otp> findById(Long id);

    // 4. OTHER METHODS
    @Update("UPDATE auth.otps SET is_used = true WHERE id = #{id}")
    int markAsUsed(Long id);

    @Delete("DELETE FROM auth.otps WHERE email = #{email}")
    int deleteByEmail(String email);

    @Delete("DELETE FROM auth.otps WHERE expires_at < CURRENT_TIMESTAMP")
    int deleteExpiredOtps();
}