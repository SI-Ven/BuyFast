package com.example.buyfast.modules.product.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.product.dto.CreateProductRequest;
import com.example.buyfast.modules.product.dto.ProductResponse;
import com.example.buyfast.modules.product.dto.UpdateProductRequest;
import com.example.buyfast.modules.product.model.Product;
import com.example.buyfast.modules.product.search.ProductDocument;
import com.example.buyfast.modules.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    // Removed StorageService dependency - it is no longer needed here!

    @PostMapping
    @Operation(summary = "Create a new product with variants")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct( // Change return type here
                                                                       @Valid @RequestBody CreateProductRequest request,
                                                                       @AuthenticationPrincipal UserDetails sellerDetails) {

        ProductResponse response = productService.createProduct(request, sellerDetails); // Change variable here

        return new ResponseEntity<>(
                ApiResponse.created("Product created successfully.", response),
                HttpStatus.CREATED
        );
    }


    @DeleteMapping("/{productUuid}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<ApiResponse<Object>> deleteProduct(
            @PathVariable UUID productUuid,
            @AuthenticationPrincipal UserDetails sellerDetails) {

        productService.deleteProduct(productUuid, sellerDetails);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully."));
    }
    @GetMapping("/{productUuid}")
    @Operation(summary = "Get a specific product details")
    public ResponseEntity<ApiResponse<ProductResponse>> getMyProduct(
            @PathVariable UUID productUuid,
            @AuthenticationPrincipal UserDetails sellerDetails) {

        // Now calling the method that returns the full DTO
        ProductResponse response = productService.getMyProduct(productUuid, sellerDetails);

        return ResponseEntity.ok(ApiResponse.success("Product retrieved.", response));
    }

    @PutMapping("/{productUuid}")
    @Operation(summary = "Update product main info")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID productUuid,
            @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal UserDetails sellerDetails) {

        ProductResponse response = productService.updateProduct(productUuid, request, sellerDetails);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully.", response));
    }

    @GetMapping("/public")
    @Operation(summary = "Get all products for Home Page (Public)")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<ProductResponse> products = productService.getAllProductsForHome(page, size);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully.", products));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products like Alibaba (Elasticsearch)")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> searchProducts(
            @RequestParam String keyword) {

        List<ProductDocument> results = productService.searchProducts(keyword);
        return ResponseEntity.ok(ApiResponse.success("Search results found", results));
    }

    @PostMapping(value = "/search/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Search products by Image (Vector Search)")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProductsByImage(
            @RequestParam("image") MultipartFile image) {

        List<ProductResponse> results = productService.searchProductsByImage(image);
        return ResponseEntity.ok(ApiResponse.success("Image search results found", results));
    }
}