package com.example.buyfast.modules.product.service.Impl;

import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.repository.CategoryRepo;
import com.example.buyfast.modules.product.dto.CreateProductRequest;
import com.example.buyfast.modules.product.dto.OptionValueRequest;
import com.example.buyfast.modules.product.dto.VariantRequest;
// import com.example.buyfast.modules.product.dto.UpdateProductRequest;
import com.example.buyfast.modules.product.model.*;
import com.example.buyfast.modules.product.repository.*;
import com.example.buyfast.modules.product.service.ProductService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final UuidService uuidService;
    // --- NEW REPOS ---
    private final ProductOptionRepo productOptionRepo;
    private final ProductOptionValueRepo productOptionValueRepo;
    private final ProductVariantRepo productVariantRepo;
    private final ProductVariantValuesRepo productVariantValuesRepo;

    // src/main/java/com/example/buyfast/modules/product/service/impl/ProductServiceImpl.java

// ... imports ...

    @Override
    @Transactional
    public Product createProduct(CreateProductRequest request, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;

        // 1. --- HANDLE CATEGORY ---
        // This finds the category by the UUID you send in the JSON
        Category category = categoryRepo.findByCategoryUuid(request.getCategoryUuid())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with UUID: " + request.getCategoryUuid()));

        // 2. Create and Save Parent Product
        Product product = new Product();
        product.setProductUuid(uuidService.generateUuid());
        product.setProductName(request.getProductName());
        product.setSellerId(seller.getId());
        product.setCompanyId(seller.getCompanyId());
        product.setCategoryId(category.getId()); // <--- Linked correctly here
        product.setDescription(request.getDescription());
        product.setActive(true);

        productRepo.insert(product); // Assuming you have an insert method

        // 3. Process Variants
        for (VariantRequest variantReq : request.getVariants()) {

            // Create Variant (SKU)
            ProductVariant variant = new ProductVariant();
            variant.setVariantUuid(uuidService.generateUuid());
            variant.setProductId(product.getId());
            variant.setPrice(variantReq.getPrice());
            variant.setStockQuantity(variantReq.getStockQuantity());
            variant.setActive(true);
            // Generate a SKU string (e.g., "PROD-123-WOODEN-SMALL")
            // variant.setSku(...);

            productVariantRepo.insert(variant);

            // 4. Process Options (Material: Wooden, Size: Small)
            for (OptionValueRequest optionReq : variantReq.getOptions()) {

                // A. Get or Create Option Group (e.g., "Material")
                ProductOption option = productOptionRepo.findByProductIdAndOptionName(product.getId(), optionReq.getOptionName())
                        .orElseGet(() -> {
                            ProductOption newOpt = new ProductOption();
                            newOpt.setOptionUuid(uuidService.generateUuid());
                            newOpt.setProductId(product.getId());
                            newOpt.setOptionName(optionReq.getOptionName());
                            productOptionRepo.insert(newOpt);
                            return newOpt;
                        });

                // B. Get or Create Option Value (e.g., "Wooden")
                ProductOptionValue value = productOptionValueRepo.findByOptionIdAndValueName(option.getId(), optionReq.getValueName())
                        .orElseGet(() -> {
                            ProductOptionValue newVal = new ProductOptionValue();
                            newVal.setValueUuid(uuidService.generateUuid());
                            newVal.setOptionId(option.getId());
                            newVal.setValueName(optionReq.getValueName());
                            productOptionValueRepo.insert(newVal);
                            return newVal;
                        });

                // C. Link Variant to this Value
                productVariantValuesRepo.insert(variant.getId(), value.getId());

                // D. --- SAVE IMAGES FOR THIS VALUE ---
                // If the user provided images for "Wooden", save them now.
                if (optionReq.getImageUrls() != null && !optionReq.getImageUrls().isEmpty()) {
                    for (String url : optionReq.getImageUrls()) {
                        // Check if this image was already saved for this specific "Wooden" value
                        // (To prevent duplicates if "Wooden" is used in multiple variants)
                        if (!productImageRepo.existsByUrlAndValueId(url, value.getId())) {
                            ProductImage image = new ProductImage();
                            image.setImageUuid(uuidService.generateUuid());
                            image.setProductId(product.getId());
                            image.setImageUrl(url);
                            image.setIsMain(false);
                            image.setOptionValueId(value.getId()); // <--- LINKED HERE

                            productImageRepo.insert(image);
                        }
                    }
                }
            }
        }

        return product;
    }

    @Override
    @Transactional
    public void deleteProduct(UUID productUuid, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;

        // Check ownership first
        Product product = productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found or you do not have permission to delete it."));

        // Deleting the product will cascade and delete all its options, values, and variants
        productRepo.deleteByUuidAndSellerId(product.getProductUuid(), seller.getId());
    }

    @Override
    public Product getMyProduct(UUID productUuid, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;
        // Note: This only returns the base product.
        // A full implementation would also fetch all its variants.
        return productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found or you do not have permission to view it."));
    }

    @Override
    public List<Product> getMyProducts(UserDetails sellerDetails) {
        User seller = (User) sellerDetails;
        // Note: This only returns a list of base products.
        return productRepo.findAllBySellerId(seller.getId());
    }
}