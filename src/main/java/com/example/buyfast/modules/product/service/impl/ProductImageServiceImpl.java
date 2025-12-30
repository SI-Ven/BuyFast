package com.example.buyfast.modules.product.service.impl;

import com.example.buyfast.modules.product.model.Product;
import com.example.buyfast.modules.product.model.ProductImage;
import com.example.buyfast.modules.product.model.ProductVariant;
import com.example.buyfast.modules.product.repository.ProductImageRepo;
import com.example.buyfast.modules.product.repository.ProductRepo;
import com.example.buyfast.modules.product.repository.ProductVariantRepo;
import com.example.buyfast.modules.product.service.ProductImageService;
import com.example.buyfast.modules.storage.service.StorageService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepo productImageRepo;
    private final ProductRepo productRepo;
    private final ProductVariantRepo productVariantRepo;
    private final StorageService storageService;
    private final UuidService uuidService;

    @Override
    @Transactional
    public ProductImage addImageToProduct(UUID productUuid, MultipartFile file, boolean isMain, int sortOrder, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;

        // 1. Verify seller owns the product
        Product product = productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found or you do not have permission."));

        // 2. Upload file
        String imageUrl = storageService.uploadFile(file);

        // 3. Save image record
        ProductImage image = new ProductImage();
        image.setImageUuid(uuidService.generateUuid());
        image.setProductId(product.getId());
        image.setImageUrl(imageUrl);
        image.setIsMain(isMain);
        image.setSortOrder(sortOrder);
        image.setVariantId(null); // This is a general product image

        productImageRepo.insert(image);
        return image;
    }

    @Override
    @Transactional
    public ProductImage addImageToVariant(UUID variantUuid, MultipartFile file, boolean isMain, int sortOrder, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;

        // 1. Find the variant by its public UUID
        ProductVariant variant = productVariantRepo.findByUuid(variantUuid)
                .orElseThrow(() -> new IllegalStateException("Product variant not found."));

        // 2. Find the parent product
        Product product = productRepo.findById(variant.getProductId())
                .orElseThrow(() -> new IllegalStateException("Parent product not found."));

        // 3. Verify this seller owns the parent product
        if (!product.getSellerId().equals(seller.getId())) {
            throw new IllegalStateException("You do not have permission to modify this product's variants.");
        }

        // 4. Upload file
        String imageUrl = storageService.uploadFile(file);

        // 5. Save image record, linking both product and variant
        ProductImage image = new ProductImage();
        image.setImageUuid(uuidService.generateUuid());
        image.setProductId(product.getId()); // Link to parent product
        image.setImageUrl(imageUrl);
        image.setIsMain(isMain);
        image.setSortOrder(sortOrder);
        image.setVariantId(variant.getId()); // <-- Link to the specific variant

        productImageRepo.insert(image);
        return image;
    }

    @Override
    @Transactional
    public void deleteImage(UUID imageUuid, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;

        // 1. Find the image
        ProductImage image = productImageRepo.findByUuid(imageUuid)
                .orElseThrow(() -> new IllegalStateException("Image not found."));

        // 2. Verify seller owns the parent product
        Product product = productRepo.findById(image.getProductId())
                .orElseThrow(() -> new IllegalStateException("Parent product not found."));

        if (!product.getSellerId().equals(seller.getId())) {
            throw new IllegalStateException("You do not have permission to delete this image.");
        }

        // 3. Delete from DB
        // In a real app, you would also delete from Pinata/S3 here
        productImageRepo.deleteByUuid(imageUuid);
    }
}