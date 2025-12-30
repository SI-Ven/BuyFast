package com.example.buyfast.modules.category.repository;

import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.model.MainCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface CategoryRepo {

    @Insert("INSERT INTO main_category (main_category_uuid, main_category_name, description, icon_url, status) " +
            "VALUES (#{mainCategoryUuid}, #{mainCategoryName}, #{description}, #{iconUrl}, 'active')")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertMainCategory(MainCategory mainCategory);

    @Select("SELECT id FROM main_category WHERE main_category_uuid = #{mainCategoryUuid}")
    Long findMainCategoryIdByUuid(UUID mainCategoryUuid);

    @Insert("INSERT INTO category (category_uuid, category_name, description, icon_url, main_category_id, level) " +
            "VALUES (#{categoryUuid}, #{categoryName}, #{description}, #{iconUrl}, #{mainCategoryId}, 2)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertCategory(Category category);

    @Select("SELECT id FROM category WHERE category_uuid = #{categoryUuid}")
    Long findCategoryIdByUuid(UUID categoryUuid);

    @Select("SELECT * FROM category WHERE category_uuid = #{categoryUuid}")
    Optional<Category> findByCategoryUuid(UUID categoryUuid);

    // --- ADDED THIS METHOD TO FIX YOUR ERROR ---
    @Select("SELECT * FROM category WHERE id = #{id}")
    Optional<Category> findById(Long id);
    // ------------------------------------------

    @Select("SELECT * FROM main_category WHERE status = 'active' ORDER BY main_category_name")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "subCategories", column = "id",
                    many = @Many(select = "com.example.buyfast.modules.category.repository.CategoryRepo.findSubCategoriesByMainId"))
    })
    List<MainCategory> findAllMainCategoriesWithSubCategories();

    @Select("SELECT * FROM category WHERE main_category_id = #{mainCategoryId} ORDER BY category_name")
    List<Category> findSubCategoriesByMainId(Long mainCategoryId);

    @Select("SELECT * FROM category ORDER BY category_name")
    List<Category> findAllSubCategories();

    @Select("SELECT main_category_name FROM main_category WHERE id = #{id}")
    String findMainCategoryNameById(Long id);
}