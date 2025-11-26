package com.example.buyfast.modules.product.service.impl;

import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.repository.CategoryRepo;
import com.example.buyfast.modules.product.dto.CreateProductRequest;
import com.example.buyfast.modules.product.dto.OptionValueRequest;
import com.example.buyfast.modules.product.dto.ProductResponse;
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

import java.util.ArrayList;
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
    public ProductResponse createProduct(CreateProductRequest request, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;

        // 1. Verify Category
        Category category = categoryRepo.findByCategoryUuid(request.getCategoryUuid())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // 2. Save Parent Product
        Product product = new Product();
        product.setProductUuid(uuidService.generateUuid());
        product.setProductName(request.getProductName());
        product.setSellerId(seller.getId());
        product.setCompanyId(seller.getCompanyId());
        product.setCategoryId(category.getId());
        product.setDescription(request.getDescription());
        product.setActive(true);
        productRepo.insert(product);

        // Prepare lists for Response
        List<ProductResponse.VariantResponse> variantResponses = new ArrayList<>();
        boolean mainImageSet = false; // <--- FIX FOR MAIN IMAGE

        // 3. Process Variants
        for (VariantRequest variantReq : request.getVariants()) {
            ProductVariant variant = new ProductVariant();
            variant.setVariantUuid(uuidService.generateUuid());
            variant.setProductId(product.getId());
            variant.setPrice(variantReq.getPrice());
            variant.setStockQuantity(variantReq.getStockQuantity());
            variant.setActive(true);

            // Generate simple SKU
            String sku = request.getProductName().substring(0, 3).toUpperCase() + "-" + variant.getVariantUuid().toString().substring(0, 8);
            variant.setSku(sku);

            productVariantRepo.insert(variant);

            List<ProductResponse.OptionResponse> optionResponses = new ArrayList<>();

            // 4. Process Options
            for (OptionValueRequest optionReq : variantReq.getOptions()) {
                // A. Save/Get Option (e.g., "Material")
                ProductOption option = productOptionRepo.findByProductIdAndOptionName(product.getId(), optionReq.getOptionName())
                        .orElseGet(() -> {
                            ProductOption newOpt = new ProductOption();
                            newOpt.setOptionUuid(uuidService.generateUuid());
                            newOpt.setProductId(product.getId());
                            newOpt.setOptionName(optionReq.getOptionName());
                            productOptionRepo.insert(newOpt);
                            return newOpt;
                        });

                // B. Save/Get Value (e.g., "Wooden")
                ProductOptionValue value = productOptionValueRepo.findByOptionIdAndValueName(option.getId(), optionReq.getValueName())
                        .orElseGet(() -> {
                            ProductOptionValue newVal = new ProductOptionValue();
                            newVal.setValueUuid(uuidService.generateUuid());
                            newVal.setOptionId(option.getId());
                            newVal.setValueName(optionReq.getValueName());
                            productOptionValueRepo.insert(newVal);
                            return newVal;
                        });

                productVariantValuesRepo.insert(variant.getId(), value.getId());

                // C. Save Images & Handle Main Image
                List<String> savedImageUrls = new ArrayList<>();
                if (optionReq.getImgUrls() != null) {
                    for (String url : optionReq.getImgUrls()) {
                        if (!productImageRepo.existsByUrlAndValueId(url, value.getId())) {
                            ProductImage image = new ProductImage();
                            image.setImageUuid(uuidService.generateUuid());
                            image.setProductId(product.getId());
                            image.setImageUrl(url);
                            image.setOptionValueId(value.getId());
                            image.setVariantId(variant.getId()); // Link to variant too for safety

                            // FIX: Set first image as main, others as false
                            if (!mainImageSet) {
                                image.setIsMain(true);
                                mainImageSet = true;
                            } else {
                                image.setIsMain(false);
                            }

                            productImageRepo.insert(image);
                            savedImageUrls.add(url);
                        }
                    }
                }

                // Add to Option Response
                optionResponses.add(ProductResponse.OptionResponse.builder()
                        .optionName(option.getOptionName())
                        .valueName(value.getValueName())
                        .images(savedImageUrls)
                        .build());
            }

            // Add to Variant Response
            variantResponses.add(ProductResponse.VariantResponse.builder()
                    .variantUuid(variant.getVariantUuid())
                    .price(variant.getPrice())
                    .stockQuantity(variant.getStockQuantity())
                    .sku(variant.getSku())
                    .options(optionResponses)
                    .build());
        }

        // 5. Construct Final Response
        return ProductResponse.builder()
                .id(product.getId())
                .productUuid(product.getProductUuid())
                .productName(product.getProductName())
                .description(product.getDescription())
                .categoryId(product.getCategoryId())
                .isActive(product.isActive())
                .variants(variantResponses)
                .build();
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