package com.example.buyfast.modules.verify.repository;

import com.example.buyfast.modules.verify.model.Verify;
import org.apache.ibatis.annotations.*;

import java.util.List; // <-- NEW IMPORT
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface VerifyRepo {

    // ... (existing methods createVerification, findByUuid, findPendingRequest, updateVerificationStatus are unchanged) ...
    @Insert("INSERT INTO verify (verify_uuid, target_type, target_id, submitted_by, verify_documents, status, created_at) " +
            "VALUES (#{verifyUuid}, #{targetType}, #{targetId}, #{submittedBy}, #{verifyDocuments}::jsonb, 'pending', CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void createVerification(Verify verify);

    @Select("SELECT * FROM verify WHERE verify_uuid = #{verifyUuid}")
    Optional<Verify> findByUuid(UUID verifyUuid);

    @Select("SELECT * FROM verify WHERE target_id = #{targetId} AND target_type = #{targetType} AND status = 'pending'")
    Optional<Verify> findPendingRequest(@Param("targetId") UUID targetId, @Param("targetType") String targetType);


    @Update("UPDATE verify SET " +
            "status = #{status}, " +
            "reviewed_by = #{reviewedBy}, " +
            "remarks = #{remarks}, " +
            "reviewed_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    void updateVerificationStatus(Verify verify);

    // --- NEW METHODS FOR SUPER ADMIN ---

    /**
     * (Admin) Gets all verification requests (pending, approved, rejected).
     */
    @Select("SELECT * FROM verify ORDER BY created_at DESC")
    List<Verify> findAllVerificationRequests();

    /**
     * (Admin) Gets only the pending verification requests.
     */
    @Select("SELECT * FROM verify WHERE status = 'pending' ORDER BY created_at ASC")
    List<Verify> findAllPendingVerificationRequests();
}