package com.example.buyfast.modules.product.service.impl;

import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.repository.CategoryRepo;
import com.example.buyfast.modules.product.dto.CreateProductRequest;
import com.example.buyfast.modules.product.dto.OptionValueRequest;
import com.example.buyfast.modules.product.dto.VariantRequest;
import com.example.buyfast.modules.product.model.*;
import com.example.buyfast.modules.product.repository.*;
import com.example.buyfast.modules.product.service.ProductService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final ProductImageRepo productImageRepo;

    @Override
    @Transactional
    public Product createProduct(CreateProductRequest request, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;

        // 1. --- HANDLE CATEGORY ---
        Category category = categoryRepo.findByCategoryUuid(request.getCategoryUuid())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with UUID: " + request.getCategoryUuid()));

        // 2. Create and Save Parent Product
        Product product = new Product();
        product.setProductUuid(uuidService.generateUuid());
        product.setProductName(request.getProductName());
        product.setSellerId(seller.getId());
        product.setCompanyId(seller.getCompanyId());
        product.setCategoryId(category.getId());
        product.setDescription(request.getDescription());
        product.setActive(true);

        productRepo.insert(product);

        // 3. Process Variants
        for (VariantRequest variantReq : request.getVariants()) {
            ProductVariant variant = new ProductVariant();
            variant.setVariantUuid(uuidService.generateUuid());
            variant.setProductId(product.getId());
            variant.setPrice(variantReq.getPrice());
            variant.setStockQuantity(variantReq.getStockQuantity());
            variant.setActive(true);

            productVariantRepo.insert(variant);

            // 4. Process Options
            for (OptionValueRequest optionReq : variantReq.getOptions()) {
                // A. Get or Create Option Group
                ProductOption option = productOptionRepo.findByProductIdAndOptionName(product.getId(), optionReq.getOptionName())
                        .orElseGet(() -> {
                            ProductOption newOpt = new ProductOption();
                            newOpt.setOptionUuid(uuidService.generateUuid());
                            newOpt.setProductId(product.getId());
                            newOpt.setOptionName(optionReq.getOptionName());
                            productOptionRepo.insert(newOpt);
                            return newOpt;
                        });

                // B. Get or Create Option Value
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
                if (optionReq.getImgUrls() != null && !optionReq.getImgUrls().isEmpty()) {
                    for (String url : optionReq.getImgUrls()) {
                        // Prevent duplicates
                        if (!productImageRepo.existsByUrlAndValueId(url, value.getId())) {
                            ProductImage image = new ProductImage();
                            image.setImageUuid(uuidService.generateUuid());
                            image.setProductId(product.getId());
                            image.setImageUrl(url);
                            image.setIsMain(false);
                            image.setOptionValueId(value.getId());

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
        Product product = productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found or permission denied."));
        productRepo.deleteByUuidAndSellerId(product.getProductUuid(), seller.getId());
    }

    @Override
    public Product getMyProduct(UUID productUuid, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;
        return productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found or permission denied."));
    }

    @Override
    public List<Product> getMyProducts(UserDetails sellerDetails) {
        User seller = (User) sellerDetails;
        return productRepo.findAllBySellerId(seller.getId());
    }
}