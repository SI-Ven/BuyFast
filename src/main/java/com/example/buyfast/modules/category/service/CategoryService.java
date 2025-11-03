package com.example.buyfast.modules.category.service;

import com.example.buyfast.modules.category.dto.CreateCategoryRequest;
import com.example.buyfast.modules.category.dto.CreateMainCategoryRequest;
import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.model.MainCategory;

public interface CategoryService {
    MainCategory createMainCategory(CreateMainCategoryRequest request);
    Category createCategory(CreateCategoryRequest request);
}