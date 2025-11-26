package com.example.buyfast.modules.product.service.impl;

import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.repository.CategoryRepo;
import com.example.buyfast.modules.product.dto.*;
import com.example.buyfast.modules.product.model.*;
import com.example.buyfast.modules.product.repository.*;
import com.example.buyfast.modules.product.service.ProductService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

        // --- PREPARE VARIABLES FOR RESPONSE & HINTS ---
        List<ProductResponse.VariantResponse> variantResponses = new ArrayList<>();
        boolean mainImageSet = false; // Tracks if we have set a main image yet

        // New Hint Variables
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        Map<String, Set<String>> optionsSummary = new HashMap<>();
        // ----------------------------------------------

        // 3. Process Variants
        for (VariantRequest variantReq : request.getVariants()) {

            // --- A. CALCULATE PRICE RANGE HINTS ---
            BigDecimal price = variantReq.getPrice();
            if (minPrice == null || price.compareTo(minPrice) < 0) {
                minPrice = price;
            }
            if (maxPrice == null || price.compareTo(maxPrice) > 0) {
                maxPrice = price;
            }
            // --------------------------------------

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

                // --- B. COLLECT OPTION HINTS (e.g. "Material": ["Wood", "Steel"]) ---
                optionsSummary.computeIfAbsent(optionReq.getOptionName(), k -> new HashSet<>())
                        .add(optionReq.getValueName());
                // --------------------------------------------------------------------

                // Save/Get Option (e.g., "Material")
                ProductOption option = productOptionRepo.findByProductIdAndOptionName(product.getId(), optionReq.getOptionName())
                        .orElseGet(() -> {
                            ProductOption newOpt = new ProductOption();
                            newOpt.setOptionUuid(uuidService.generateUuid());
                            newOpt.setProductId(product.getId());
                            newOpt.setOptionName(optionReq.getOptionName());
                            productOptionRepo.insert(newOpt);
                            return newOpt;
                        });

                // Save/Get Value (e.g., "Wooden")
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

                // Save Images & Handle Main Image
                List<String> savedImageUrls = new ArrayList<>();
                if (optionReq.getImgUrls() != null) {
                    for (String url : optionReq.getImgUrls()) {
                        // Avoid duplicates for the same value
                        if (!productImageRepo.existsByUrlAndValueId(url, value.getId())) {
                            ProductImage image = new ProductImage();
                            image.setImageUuid(uuidService.generateUuid());
                            image.setProductId(product.getId());
                            image.setImageUrl(url);
                            image.setOptionValueId(value.getId());
                            image.setVariantId(variant.getId());

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

        // 5. Construct Final Response with Hints
        return ProductResponse.builder()
                .id(product.getId())
                .productUuid(product.getProductUuid())
                .productName(product.getProductName())
                .description(product.getDescription())
                // --- SET HINTS HERE ---
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .availableOptions(optionsSummary)
                // ----------------------
                .categoryId(product.getCategoryId())
                .isActive(product.isActive())
                .variants(variantResponses)
                .build();
    }

    @Override
    public ProductResponse getMyProduct(UUID productUuid, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;

        // A. Get Parent Product
        Product product = productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        // B. Get All Variants
        List<ProductVariant> variants = productVariantRepo.findAllByProductId(product.getId());

        // Hints Calculation
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        Map<String, Set<String>> optionsSummary = new HashMap<>();

        List<ProductResponse.VariantResponse> variantResponses = new ArrayList<>();

        // C. Loop Variants to build structure
        for (ProductVariant variant : variants) {

            // Calc Price Range
            if (minPrice == null || variant.getPrice().compareTo(minPrice) < 0) minPrice = variant.getPrice();
            if (maxPrice == null || variant.getPrice().compareTo(maxPrice) > 0) maxPrice = variant.getPrice();

            // Fetch Values linked to this Variant (Using the Repo method we added)
            List<ProductOptionValue> values = productVariantValuesRepo.findValuesByVariantId(variant.getId());

            List<ProductResponse.OptionResponse> optionResponses = new ArrayList<>();

            for (ProductOptionValue val : values) {
                // Fetch Option Name (e.g., "Material")
                ProductOption option = productOptionRepo.findById(val.getOptionId()); // Ensure you have findById in Repo

                // Fetch Images for this Value (e.g., Wooden images)
                List<ProductImage> images = productImageRepo.findAllByOptionValueId(val.getId());
                List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());

                // Add to Summary for Hints
                optionsSummary.computeIfAbsent(option.getOptionName(), k -> new HashSet<>()).add(val.getValueName());

                optionResponses.add(ProductResponse.OptionResponse.builder()
                        .optionName(option.getOptionName())
                        .valueName(val.getValueName())
                        .images(imageUrls)
                        .build());
            }

            variantResponses.add(ProductResponse.VariantResponse.builder()
                    .variantUuid(variant.getVariantUuid())
                    .price(variant.getPrice())
                    .stockQuantity(variant.getStockQuantity())
                    .sku(variant.getSku())
                    .options(optionResponses)
                    .build());
        }

        return ProductResponse.builder()
                .id(product.getId())
                .productUuid(product.getProductUuid())
                .productName(product.getProductName())
                .description(product.getDescription())
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .availableOptions(optionsSummary)
                .categoryId(product.getCategoryId())
                .isActive(product.isActive())
                .variants(variantResponses)
                .build();
    }

    // =================================================================
    // 2. UPDATE PRODUCT (Updates Main Info)
    // =================================================================
    @Override
    @Transactional
    public ProductResponse updateProduct(UUID productUuid, UpdateProductRequest request, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;
        Product product = productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        // Update fields if they are not null
        if (request.getProductName() != null) product.setProductName(request.getProductName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());

        // NOTE: Updating variants is complex. Usually, we recommend deleting
        // the old product and creating a new one if the variants change completely.
        // Or you can add specific "updateVariant" methods later.

        productRepo.update(product); // Ensure you have an 'update' method in ProductRepo

        // Return the updated full response
        return getMyProduct(productUuid, sellerDetails);
    }

    // =================================================================
    // 3. DELETE PRODUCT (Cascade handles everything!)
    // =================================================================
    @Override
    @Transactional
    public void deleteProduct(UUID productUuid, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;

        // 1. Check ownership
        Product product = productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found or permission denied."));

        // 2. Delete Parent
        // Because your Schema.sql has "ON DELETE CASCADE" on variants, options, images,
        // deleting this ONE row will automatically remove ALL related data from the DB.
        productRepo.deleteByUuidAndSellerId(product.getProductUuid(), seller.getId());
    }
}