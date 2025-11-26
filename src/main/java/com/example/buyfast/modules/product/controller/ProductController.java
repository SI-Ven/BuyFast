package com.example.buyfast.modules.product.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.product.dto.CreateProductRequest;
import com.example.buyfast.modules.product.dto.ProductResponse;
import com.example.buyfast.modules.product.model.Product;
import com.example.buyfast.modules.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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

    @GetMapping
    @Operation(summary = "Get all products for the logged-in seller")
    public ResponseEntity<ApiResponse<List<Product>>> getMyProducts(
            @AuthenticationPrincipal UserDetails sellerDetails) {

        List<Product> products = productService.getMyProducts(sellerDetails);
        return ResponseEntity.ok(ApiResponse.success("Retrieved all my products.", products));
    }

    @GetMapping("/{productUuid}")
    @Operation(summary = "Get a specific product by UUID")
    public ResponseEntity<ApiResponse<Product>> getMyProduct(
            @PathVariable UUID productUuid,
            @AuthenticationPrincipal UserDetails sellerDetails) {

        Product product = productService.getMyProduct(productUuid, sellerDetails);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved.", product));
    }

    @DeleteMapping("/{productUuid}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<ApiResponse<Object>> deleteProduct(
            @PathVariable UUID productUuid,
            @AuthenticationPrincipal UserDetails sellerDetails) {

        productService.deleteProduct(productUuid, sellerDetails);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully."));
    }
}