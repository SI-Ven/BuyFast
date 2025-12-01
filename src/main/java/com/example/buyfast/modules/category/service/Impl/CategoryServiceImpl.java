package com.example.buyfast.modules.category.service.Impl;


import com.example.buyfast.modules.category.dto.CreateCategoryRequest;
import com.example.buyfast.modules.category.dto.CreateMainCategoryRequest;
import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.model.MainCategory;
import com.example.buyfast.modules.category.repository.CategoryRepo;
import com.example.buyfast.modules.category.service.CategoryService;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private final UuidService uuidService;

    @Transactional
    @Override
    @CacheEvict(value = "mainCategories", allEntries = true) // <--- Clears the old cache
    public MainCategory createMainCategory(CreateMainCategoryRequest request) {
        // ... (Unchanged)
        MainCategory mainCategory = new MainCategory();
        mainCategory.setMainCategoryUuid(uuidService.generateUuid());
        mainCategory.setMainCategoryName(request.getMainCategoryName());
        mainCategory.setDescription(request.getDescription());
        mainCategory.setIconUrl(request.getIconUrl());
        categoryRepo.insertMainCategory(mainCategory);
        return mainCategory;
    }

    @Transactional
    @Override
    @CacheEvict(value = "mainCategories", allEntries = true) // <--- Clears the old cache
    public Category createCategory(CreateCategoryRequest request) {
        // ... (Unchanged)
        Long parentInternalId = categoryRepo.findMainCategoryIdByUuid(request.getMainCategoryUuid());

        if (parentInternalId == null) {
            throw new RuntimeException("Parent main category not found with UUID: " + request.getMainCategoryUuid());
        }

        Category category = new Category();
        category.setCategoryUuid(uuidService.generateUuid());
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        category.setIconUrl(request.getIconUrl());
        category.setMainCategoryId(parentInternalId);
        category.setLevel(2);
        categoryRepo.insertCategory(category);
        return category;
    }

    // --- MODIFIED METHOD ---
    @Override
    @Cacheable(value = "mainCategories", key = "'tree'")
    public List<MainCategory> getAllMainCategoriesWithSubCategories() {
        // This one call now does all the work
        return categoryRepo.findAllMainCategoriesWithSubCategories();
    }

    @Override
    public List<Category> getAllSubCategories() {
        // (Unchanged)
        return categoryRepo.findAllSubCategories();
    }
}