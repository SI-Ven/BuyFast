package com.example.buyfast.modules.company.repository;

import com.example.buyfast.modules.company.model.Company;
import org.apache.ibatis.annotations.*;

import java.util.Optional;
import java.util.UUID; // <-- NEW IMPORT

@Mapper
public interface CompanyRepo {

    @Insert("INSERT INTO company (company_uuid, company_name, industry_type, logo_url, description, created_by, status, max_sellers, " +
            "address_line_1, city, state_province, postal_code, country) " +
            "VALUES (#{companyUuid}, #{companyName}, #{industryType}, #{logoUrl}, #{description}, #{createdBy}, #{status}, #{maxSellers}, " + // <-- Use #{status}
            "#{addressLine1}, #{city}, #{stateProvince}, #{postalCode}, #{country})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(Company company);

    @Select("SELECT * FROM company WHERE created_by = #{adminUserId}")
    Optional<Company> findByAdminId(Long adminUserId);

    @Select("SELECT * FROM company WHERE id = #{companyId}")
    Optional<Company> findById(Long companyId);

    // --- NEW METHOD ---
    @Select("SELECT * FROM company WHERE company_uuid = #{companyUuid}")
    Optional<Company> findByUuid(UUID companyUuid);

    // --- NEW METHOD ---
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
}