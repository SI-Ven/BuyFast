package com.example.buyfast.modules.product.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.product.model.ProductImage;
import com.example.buyfast.modules.product.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products") // We use the same base URL
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    /**
     * Uploads a general image for a product.
     */
    @PostMapping(value = "/{productUuid}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductImage>> addImageToProduct(
            @PathVariable UUID productUuid,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "isMain", defaultValue = "false") boolean isMain,
            @RequestParam(value = "sortOrder", defaultValue = "0") int sortOrder,
            @AuthenticationPrincipal UserDetails sellerDetails) {

        ProductImage image = productImageService.addImageToProduct(productUuid, file, isMain, sortOrder, sellerDetails);
        return new ResponseEntity<>(
                ApiResponse.created("Image added to product.", image),
                HttpStatus.CREATED
        );
    }

    /**
     * Uploads a specific image for a variant (e.g., a "Red" shirt).
     */
    @PostMapping(value = "/variants/{variantUuid}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductImage>> addImageToVariant(
            @PathVariable UUID variantUuid,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "isMain", defaultValue = "false") boolean isMain,
            @RequestParam(value = "sortOrder", defaultValue = "0") int sortOrder,
            @AuthenticationPrincipal UserDetails sellerDetails) {

        ProductImage image = productImageService.addImageToVariant(variantUuid, file, isMain, sortOrder, sellerDetails);
        return new ResponseEntity<>(
                ApiResponse.created("Image added to variant.", image),
                HttpStatus.CREATED
        );
    }

    /**
     * Deletes any product image (general or variant).
     */
    @DeleteMapping("/images/{imageUuid}")
    public ResponseEntity<ApiResponse<Object>> deleteImage(
            @PathVariable UUID imageUuid,
            @AuthenticationPrincipal UserDetails sellerDetails) {

        productImageService.deleteImage(imageUuid, sellerDetails);
        return ResponseEntity.ok(ApiResponse.ok("Image deleted successfully."));
    }
}