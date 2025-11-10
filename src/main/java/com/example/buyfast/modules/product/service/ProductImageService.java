package com.example.buyfast.modules.product.service;

import com.example.buyfast.modules.product.model.ProductImage;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface ProductImageService {

    /**
     * Adds a general image to a base product.
     */
    ProductImage addImageToProduct(UUID productUuid, MultipartFile file, boolean isMain, int sortOrder, UserDetails sellerDetails);

    /**
     * Adds a specific image to a product variant (e.g., "Red" color).
     */
    ProductImage addImageToVariant(UUID variantUuid, MultipartFile file, boolean isMain, int sortOrder, UserDetails sellerDetails);

    /**
     * Deletes an image (from product or variant).
     */
    void deleteImage(UUID imageUuid, UserDetails sellerDetails);
}