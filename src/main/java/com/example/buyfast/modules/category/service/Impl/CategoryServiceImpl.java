package com.example.buyfast.modules.category.service.Impl;


import com.example.buyfast.modules.category.dto.CreateCategoryRequest;
import com.example.buyfast.modules.category.dto.CreateMainCategoryRequest;
import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.model.MainCategory;
import com.example.buyfast.modules.category.repository.CategoryRepo;
import com.example.buyfast.modules.category.service.CategoryService;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private final UuidService uuidService;

    @Transactional
    @Override
    public MainCategory createMainCategory(CreateMainCategoryRequest request) {
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
    public Category createCategory(CreateCategoryRequest request) {
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
}