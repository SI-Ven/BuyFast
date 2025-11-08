package com.example.buyfast.modules.category.repository;

import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.model.MainCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface CategoryRepo {

    /**
     * Inserts a new main category.
     * (Unchanged)
     */
    @Insert("INSERT INTO main_category (main_category_uuid, main_category_name, description, icon_url, status) " +
            "VALUES (#{mainCategoryUuid}, #{mainCategoryName}, #{description}, #{iconUrl}, 'active')")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertMainCategory(MainCategory mainCategory);

    /**
     * Finds the internal ID (BIGSERIAL) of a main category
     * using its external UUID.
     * (Unchanged)
     */
    @Select("SELECT id FROM main_category WHERE main_category_uuid = #{mainCategoryUuid}")
    Long findMainCategoryIdByUuid(UUID mainCategoryUuid);

    /**
     * Inserts a new sub-category.
     * (Unchanged)
     */
    @Insert("INSERT INTO category (category_uuid, category_name, description, icon_url, main_category_id, level) " +
            "VALUES (#{categoryUuid}, #{categoryName}, #{description}, #{iconUrl}, #{mainCategoryId}, 2)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertCategory(Category category);

    // --- NEW METHODS ---

    /**
     * Selects all active main categories and nests their sub-categories.
     * This replaces the old `findAllMainCategories`.
     */
    @Select("SELECT * FROM main_category WHERE status = 'active' ORDER BY main_category_name")
    @Results({
            @Result(property = "id", column = "id"), // Links main_category.id
            @Result(property = "subCategories", column = "id", // to the query parameter {mainCategoryId}
                    many = @Many(select = "com.example.buyfast.modules.category.repository.CategoryRepo.findSubCategoriesByMainId"))
    })
    List<MainCategory> findAllMainCategoriesWithSubCategories();

    /**
     * Helper method to find sub-categories for a given main category ID.
     * This is called by the @Many annotation above.
     */
    @Select("SELECT * FROM category WHERE main_category_id = #{mainCategoryId} ORDER BY category_name")
    List<Category> findSubCategoriesByMainId(Long mainCategoryId);


    /**
     * Selects all sub-categories (still useful for other admin tasks).
     * (Unchanged)
     */
    @Select("SELECT * FROM category ORDER BY category_name")
    List<Category> findAllSubCategories();
}