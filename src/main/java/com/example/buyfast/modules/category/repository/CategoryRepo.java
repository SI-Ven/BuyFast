package com.example.buyfast.modules.category.repository;

import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.model.MainCategory;
import org.apache.ibatis.annotations.*;
import java.util.UUID;

@Mapper
public interface CategoryRepo {

    /**
     * Inserts a new main category.
     * (We removed jdbcType=OTHER)
     */
    @Insert("INSERT INTO main_category (main_category_uuid, main_category_name, description, icon_url, status) " +
            "VALUES (#{mainCategoryUuid}, #{mainCategoryName}, #{description}, #{iconUrl}, 'active')")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertMainCategory(MainCategory mainCategory);

    /**
     * Finds the internal ID (BIGSERIAL) of a main category
     * using its external UUID.
     * (We removed jdbcType=OTHER)
     */
    @Select("SELECT id FROM main_category WHERE main_category_uuid = #{mainCategoryUuid}")
    Long findMainCategoryIdByUuid(UUID mainCategoryUuid);

    /**
     * Inserts a new sub-category.
     * (We removed jdbcType=OTHER)
     */
    @Insert("INSERT INTO category (category_uuid, category_name, description, icon_url, main_category_id, level) " +
            "VALUES (#{categoryUuid}, #{categoryName}, #{description}, #{iconUrl}, #{mainCategoryId}, 2)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertCategory(Category category);

}