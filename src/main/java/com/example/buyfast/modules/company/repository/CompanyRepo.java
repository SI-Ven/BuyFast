package com.example.buyfast.modules.company.repository;

import com.example.buyfast.modules.company.model.Company;
import org.apache.ibatis.annotations.*;

import java.util.List; // <-- NEW IMPORT
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface CompanyRepo {

    // ... (existing methods insert, findByAdminId, findById, findByUuid, updateCompanyStatus, updateCompanyProfile are unchanged) ...
    @Insert("INSERT INTO company (company_uuid, company_name, industry_type,tax_id, logo_url, description, created_by, status, max_sellers, " +
            "address_line_1, city, state_province, postal_code, country) " +
            "VALUES (#{companyUuid}, #{companyName}, #{industryType},#{taxId}, #{logoUrl}, #{description}, #{createdBy}, #{status}, #{maxSellers}, " +
            "#{addressLine1}, #{city}, #{stateProvince}, #{postalCode}, #{country})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(Company company);

    @Select("SELECT * FROM company WHERE created_by = #{adminUserId}")
    Optional<Company> findByAdminId(Long adminUserId);

    @Select("SELECT * FROM company WHERE id = #{companyId}")
    Optional<Company> findById(Long companyId);

    @Select("SELECT * FROM company WHERE company_uuid = #{companyUuid}")
    Optional<Company> findByUuid(UUID companyUuid);

    @Update("UPDATE company SET status = #{status} WHERE id = #{id}")
    void updateCompanyStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE company SET " +
            "description = #{description}, " +
            "logo_url = #{logoUrl}, " +
            "address_line_1 = #{addressLine1}, " +
            "city = #{city}, " +
            "state_province = #{stateProvince}, " +
            "postal_code = #{postalCode}, " +
            "country = #{country} " +
            "WHERE id = #{id}")
    void updateCompanyProfile(Company company);

    // --- NEW METHOD FOR SUPER ADMIN ---

    /**
     * (Admin) Gets a list of all companies in the system.
     */
    @Select("SELECT * FROM company ORDER BY created_at DESC")
    List<Company> findAllCompanies();
}