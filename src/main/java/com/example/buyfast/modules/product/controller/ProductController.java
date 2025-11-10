package com.example.buyfast.modules.product.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.product.dto.CreateProductRequest;
// import com.example.buyfast.modules.product.dto.UpdateProductRequest; // Update DTO is not used yet
import com.example.buyfast.modules.product.model.Product;
import com.example.buyfast.modules.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal UserDetails sellerDetails) {

        Product product = productService.createProduct(request, sellerDetails);
        return new ResponseEntity<>(
                ApiResponse.created("Product and its variants created successfully.", product),
                HttpStatus.CREATED
        );
    }

    // --- Update is now very complex, so we will disable it for now ---
    /*
    @PutMapping("/{productUuid}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable UUID productUuid,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal UserDetails sellerDetails) {

        Product product = productService.updateProduct(productUuid, request, sellerDetails);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully.", product));
    }
    */

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getMyProducts(
            @AuthenticationPrincipal UserDetails sellerDetails) {

        List<Product> products = productService.getMyProducts(sellerDetails);
        return ResponseEntity.ok(ApiResponse.success("Retrieved all my products.", products));
    }

    @GetMapping("/{productUuid}")
    public ResponseEntity<ApiResponse<Product>> getMyProduct(
            @PathVariable UUID productUuid,
            @AuthenticationPrincipal UserDetails sellerDetails) {

        Product product = productService.getMyProduct(productUuid, sellerDetails);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved.", product));
    }

    @DeleteMapping("/{productUuid}")
    public ResponseEntity<ApiResponse<Object>> deleteProduct(
            @PathVariable UUID productUuid,
            @AuthenticationPrincipal UserDetails sellerDetails) {

        productService.deleteProduct(productUuid, sellerDetails);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully."));
    }
}