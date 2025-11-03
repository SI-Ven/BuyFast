package com.example.buyfast.modules.category.repository;

import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.model.MainCategory;
import org.apache.ibatis.annotations.*;
import java.util.UUID;

@Mapper
public interface CategoryRepo {

    /**
     * Inserts a new main category.
     */
    @Insert("INSERT INTO main_category (main_category_uuid, main_category_name, description, icon_url, status) " +
            "VALUES (#{mainCategoryUuid, jdbcType=OTHER}, #{mainCategoryName}, #{description}, #{iconUrl}, 'active')")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertMainCategory(MainCategory mainCategory);

    /**
     * Finds the internal ID (BIGSERIAL) of a main category
     * using its external UUID.
     */
    @Select("SELECT id FROM main_category WHERE main_category_uuid = #{mainCategoryUuid, jdbcType=OTHER}")
    Long findMainCategoryIdByUuid(UUID mainCategoryUuid);

    /**
     * Inserts a new sub-category.
     */
    @Insert("INSERT INTO category (category_uuid, category_name, description, icon_url, main_category_id, level) " +
            "VALUES (#{categoryUuid, jdbcType=OTHER}, #{categoryName}, #{description}, #{iconUrl}, #{mainCategoryId}, 2)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertCategory(Category category);

}