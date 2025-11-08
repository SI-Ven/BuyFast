package com.example.buyfast.modules.category.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.category.dto.CreateCategoryRequest;
import com.example.buyfast.modules.category.dto.CreateMainCategoryRequest;
import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.model.MainCategory;
import com.example.buyfast.modules.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // --- MODIFIED GETTER ENDPOINT ---

    @GetMapping("/main")
    public ResponseEntity<ApiResponse<List<MainCategory>>> getAllMainCategories() {
        // This now returns the nested structure (MainCategory -> List<Category>)
        List<MainCategory> categories = categoryService.getAllMainCategoriesWithSubCategories();
        return ResponseEntity.ok(ApiResponse.success("Main categories retrieved successfully", categories));
    }

    // --- UNCHANGED ENDPOINTS ---

    @GetMapping("/sub")
    public ResponseEntity<ApiResponse<List<Category>>> getAllSubCategories() {
        List<Category> categories = categoryService.getAllSubCategories();
        return ResponseEntity.ok(ApiResponse.success("Sub-categories retrieved successfully", categories));
    }

    @PostMapping("/main")
    public ResponseEntity<ApiResponse<MainCategory>> createMainCategory(@Valid @RequestBody CreateMainCategoryRequest request) {
        MainCategory createdCategory = categoryService.createMainCategory(request);
        return new ResponseEntity<>(
                ApiResponse.created("Main category created successfully", createdCategory),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/sub")
    public ResponseEntity<ApiResponse<Category>> createSubCategory(@Valid @RequestBody CreateCategoryRequest request) {
        Category createdCategory = categoryService.createCategory(request);
        return new ResponseEntity<>(
                ApiResponse.created("Sub-category created successfully", createdCategory),
                HttpStatus.CREATED
        );
    }
}