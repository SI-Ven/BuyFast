package com.example.buyfast.modules.category.controller;

import com.example.buyfast.modules.category.dto.CreateCategoryRequest;
import com.example.buyfast.modules.category.dto.CreateMainCategoryRequest;
import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.model.MainCategory;
import com.example.buyfast.modules.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/main")
    public ResponseEntity<MainCategory> createMainCategory(@Valid @RequestBody CreateMainCategoryRequest request) {
        MainCategory createdCategory = categoryService.createMainCategory(request);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @PostMapping("/sub")
    public ResponseEntity<Category> createSubCategory(@Valid @RequestBody CreateCategoryRequest request) {
        Category createdCategory = categoryService.createCategory(request);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }
}