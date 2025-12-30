package com.example.buyfast.modules.dispute.repository;

import com.example.buyfast.modules.dispute.model.Dispute;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DisputeRepo {

    @Insert("INSERT INTO disputes (order_id, user_id, reason, description, status, created_at) " +
            "VALUES (#{orderId}, #{userId}, #{reason}, #{description}, 'OPEN', NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Dispute dispute);

    @Insert("INSERT INTO dispute_evidence (dispute_id, image_url) VALUES (#{disputeId}, #{imageUrl})")
    void saveEvidence(Long disputeId, String imageUrl);

    @Select("SELECT * FROM disputes WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "evidenceImages", column = "id",
                    many = @Many(select = "findEvidenceByDisputeId"))
    })
    Optional<Dispute> findById(Long id);

    @Select("SELECT image_url FROM dispute_evidence WHERE dispute_id = #{disputeId}")
    List<String> findEvidenceByDisputeId(Long disputeId);

    @Update("UPDATE disputes SET status = #{status}, admin_comment = #{adminComment} WHERE id = #{id}")
    void updateStatus(Long id, String status, String adminComment);
}