package com.example.buyfast.modules.address.repository;

import com.example.buyfast.config.mybatis.UuidTypeHandler;
import com.example.buyfast.modules.address.model.ShippingAddress;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface AddressRepo {
    @Update("UPDATE shipping_address SET is_default = false WHERE user_id = #{userId}")
    void removeAllDefaultsForUser(Long userId);

    @Select("""
        INSERT INTO shipping_address (address_uuid, user_id, full_name,phone_number, address_line_1, city, country, is_default)
        VALUES (#{addressUuid}, #{userId}, #{fullName},#{phoneNumber}, #{addressLine1}, #{city}, #{country}, #{isDefault})
        RETURNING *
    """)
    @Results(id = "AddressMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "addressUuid", column = "address_uuid", typeHandler = UuidTypeHandler.class),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "fullName", column = "full_name"),
            @Result(property = "phoneNumber",column = "phone_number"),
            @Result(property = "addressLine1", column = "address_line_1"),
            @Result(property = "isDefault", column = "is_default")
    })
    ShippingAddress save(ShippingAddress address);

    @Select("""
SELECT * FROM shipping_address WHERE user_id = #{userId} ORDER BY is_default DESC, id DESC
""")
    @ResultMap("AddressMap")
    List<ShippingAddress> findAllByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM shipping_address WHERE address_uuid = #{uuid} AND user_id = #{userId}")
    void deleteByUuidAndUser(@Param("uuid") UUID uuid, @Param("userId") Long userId);

    @Update("""
    UPDATE shipping_address
    SET full_name = #{fullName},
        phone_number = #{phoneNumber},
        address_line_1 = #{addressLine1},
        city = #{city},
        country = #{country},
        is_default = #{isDefault}
    WHERE address_uuid = #{addressUuid} AND user_id = #{userId}
""")
    void update(ShippingAddress address);

    @Select("SELECT * FROM shipping_address WHERE address_uuid = #{uuid} AND user_id = #{userId}")
    @ResultMap("AddressMap")
    Optional<ShippingAddress> findByUuidAndUser(@Param("uuid") UUID uuid, @Param("userId") Long userId);
}


