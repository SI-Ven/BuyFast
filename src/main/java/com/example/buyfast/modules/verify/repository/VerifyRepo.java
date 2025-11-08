package com.example.buyfast.modules.verify.repository;

import com.example.buyfast.modules.verify.model.Verify;
import org.apache.ibatis.annotations.*;

import java.util.Optional;
import java.util.UUID;

@Mapper
public interface VerifyRepo {

    @Insert("INSERT INTO verify (verify_uuid, target_type, target_id, submitted_by, verify_documents, status, created_at) " +
            "VALUES (#{verifyUuid}, #{targetType}, #{targetId}, #{submittedBy}, #{verifyDocuments}::jsonb, 'pending', CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void createVerification(Verify verify);

    @Select("SELECT * FROM verify WHERE verify_uuid = #{verifyUuid}")
    Optional<Verify> findByUuid(UUID verifyUuid);

    // --- NEW METHOD ---
    @Select("SELECT * FROM verify WHERE target_id = #{targetId} AND target_type = #{targetType} AND status = 'pending'")
    Optional<Verify> findPendingRequest(@Param("targetId") UUID targetId, @Param("targetType") String targetType);


    @Update("UPDATE verify SET " +
            "status = #{status}, " +
            "reviewed_by = #{reviewedBy}, " +
            "remarks = #{remarks}, " +
            "reviewed_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    void updateVerificationStatus(Verify verify);
}