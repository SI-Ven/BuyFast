package com.example.buyfast.modules.product.service.impl;

import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.repository.CategoryRepo;
import com.example.buyfast.modules.product.dto.*;
import com.example.buyfast.modules.product.model.*;
import com.example.buyfast.modules.product.repository.*;
import com.example.buyfast.modules.product.search.ProductDocument;
import com.example.buyfast.modules.product.search.ProductSearchRepo; // <--- 1. IMPORT ADDED
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

    // --- SEARCH REPO ---
    private final ProductSearchRepo productSearchRepo; // <--- 2. INJECTED

    // =================================================================
    // 1. CREATE PRODUCT (With Search Sync)
    // =================================================================
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
        product.setSellerId(Long.valueOf(seller.getUserUuid().toString()));
        product.setActive(true);
        productRepo.insert(product);

        // --- PREPARE VARIABLES FOR RESPONSE & HINTS ---
        List<ProductResponse.VariantResponse> variantResponses = new ArrayList<>();
        boolean mainImageSet = false;

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
            String sku = request.getProductName().substring(0, Math.min(3, request.getProductName().length())).toUpperCase()
                    + "-" + variant.getVariantUuid().toString().substring(0, 8);
            variant.setSku(sku);

            productVariantRepo.insert(variant);

            List<ProductResponse.OptionResponse> optionResponses = new ArrayList<>();

            // 4. Process Options
            for (OptionValueRequest optionReq : variantReq.getOptions()) {

                // --- B. COLLECT OPTION HINTS ---
                optionsSummary.computeIfAbsent(optionReq.getOptionName(), k -> new HashSet<>())
                        .add(optionReq.getValueName());

                // Save/Get Option
                ProductOption option = productOptionRepo.findByProductIdAndOptionName(product.getId(), optionReq.getOptionName())
                        .orElseGet(() -> {
                            ProductOption newOpt = new ProductOption();
                            newOpt.setOptionUuid(uuidService.generateUuid());
                            newOpt.setProductId(product.getId());
                            newOpt.setOptionName(optionReq.getOptionName());
                            productOptionRepo.insert(newOpt);
                            return newOpt;
                        });

                // Save/Get Value
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

                // Save Images
                List<String> savedImageUrls = new ArrayList<>();
                if (optionReq.getImgUrls() != null) {
                    for (String url : optionReq.getImgUrls()) {
                        if (!productImageRepo.existsByUrlAndValueId(url, value.getId())) {
                            ProductImage image = new ProductImage();
                            image.setImageUuid(uuidService.generateUuid());
                            image.setProductId(product.getId());
                            image.setImageUrl(url);
                            image.setOptionValueId(value.getId());
                            image.setVariantId(variant.getId());

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

                optionResponses.add(ProductResponse.OptionResponse.builder()
                        .optionName(option.getOptionName())
                        .valueName(value.getValueName())
                        .images(savedImageUrls)
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

        // --- SYNC TO ELASTICSEARCH ---
        saveToElasticsearch(product, category, minPrice);
        // -----------------------------

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

        for (ProductVariant variant : variants) {
            if (minPrice == null || variant.getPrice().compareTo(minPrice) < 0) minPrice = variant.getPrice();
            if (maxPrice == null || variant.getPrice().compareTo(maxPrice) > 0) maxPrice = variant.getPrice();

            List<ProductOptionValue> values = productVariantValuesRepo.findValuesByVariantId(variant.getId());
            List<ProductResponse.OptionResponse> optionResponses = new ArrayList<>();

            for (ProductOptionValue val : values) {
                ProductOption option = productOptionRepo.findById(val.getOptionId());
                List<ProductImage> images = productImageRepo.findAllByOptionValueId(val.getId());
                List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());

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
                .sellerId(String.valueOf(seller.getUserUuid()))
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .availableOptions(optionsSummary)
                .categoryId(product.getCategoryId())
                .isActive(product.isActive())
                .variants(variantResponses)
                .build();
    }

    // =================================================================
    // 2. UPDATE PRODUCT (Updates Main Info & Syncs Search)
    // =================================================================
    @Override
    @Transactional
    public ProductResponse updateProduct(UUID productUuid, UpdateProductRequest request, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;
        Product product = productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        // 1. Update Base Fields
        if (request.getProductName() != null) product.setProductName(request.getProductName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getIsActive() != null) product.setActive(request.getIsActive());

        // 2. Update Category
        if (request.getCategoryUuid() != null) {
            Category category = categoryRepo.findByCategoryUuid(request.getCategoryUuid())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategoryId(category.getId());
        }

        productRepo.update(product);

        // 3. Update Variants if provided
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            productImageRepo.deleteAllByProductId(product.getId());
            productVariantRepo.deleteAllByProductId(product.getId());
            productOptionRepo.deleteAllByProductId(product.getId());
            processVariants(product, request.getVariants());
        }

        // 4. SYNC TO ELASTICSEARCH
        // We need minPrice and Category Name for the search document
        Category category = categoryRepo.findById(product.getCategoryId()).orElse(new Category());

        // Recalculate minPrice for the sync
        List<ProductVariant> currentVariants = productVariantRepo.findAllByProductId(product.getId());
        BigDecimal minPrice = currentVariants.stream()
                .map(ProductVariant::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        saveToElasticsearch(product, category, minPrice);
        // -------------------------

        return getMyProduct(productUuid, sellerDetails);
    }

    private void processVariants(Product product, List<VariantRequest> variants) {
        boolean mainImageSet = false;

        for (VariantRequest variantReq : variants) {
            ProductVariant variant = new ProductVariant();
            variant.setVariantUuid(uuidService.generateUuid());
            variant.setProductId(product.getId());
            variant.setPrice(variantReq.getPrice());
            variant.setStockQuantity(variantReq.getStockQuantity());
            variant.setActive(true);

            String sku = product.getProductName().substring(0, Math.min(3, product.getProductName().length())).toUpperCase()
                    + "-" + variant.getVariantUuid().toString().substring(0, 8);
            variant.setSku(sku);

            productVariantRepo.insert(variant);

            for (OptionValueRequest optionReq : variantReq.getOptions()) {
                ProductOption option = productOptionRepo.findByProductIdAndOptionName(product.getId(), optionReq.getOptionName())
                        .orElseGet(() -> {
                            ProductOption newOpt = new ProductOption();
                            newOpt.setOptionUuid(uuidService.generateUuid());
                            newOpt.setProductId(product.getId());
                            newOpt.setOptionName(optionReq.getOptionName());
                            productOptionRepo.insert(newOpt);
                            return newOpt;
                        });

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

                if (optionReq.getImgUrls() != null) {
                    for (String url : optionReq.getImgUrls()) {
                        if (!productImageRepo.existsByUrlAndValueId(url, value.getId())) {
                            ProductImage image = new ProductImage();
                            image.setImageUuid(uuidService.generateUuid());
                            image.setProductId(product.getId());
                            image.setImageUrl(url);
                            image.setOptionValueId(value.getId());
                            image.setVariantId(variant.getId());

                            if (!mainImageSet) {
                                image.setIsMain(true);
                                mainImageSet = true;
                            } else {
                                image.setIsMain(false);
                            }

                            productImageRepo.insert(image);
                        }
                    }
                }
            }
        }
    }

    // =================================================================
    // 3. DELETE PRODUCT (Cascade handles everything + Remove from ES)
    // =================================================================
    @Override
    @Transactional
    public void deleteProduct(UUID productUuid, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;

        Product product = productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found or permission denied."));

        // Delete from Postgres
        productRepo.deleteByUuidAndSellerId(product.getProductUuid(), seller.getId());

        // --- REMOVE FROM ELASTICSEARCH ---
        try {
            productSearchRepo.deleteById(product.getId());
        } catch (Exception e) {
            System.err.println("Warning: Failed to delete product from Elasticsearch: " + e.getMessage());
        }
    }

    @Override
    public List<ProductResponse> getAllProductsForHome(int page, int size) {
        if (page < 1) page = 1;
        int offset = (page - 1) * size;
        return productRepo.findAllActiveProductsSummary(size, offset);
    }

    // =================================================================
    // 4. SEARCH PRODUCTS (ELASTICSEARCH)
    // =================================================================
    @Override
    public List<ProductDocument> searchProducts(String keyword) {
        // This leverages the Elasticsearch engine
        return productSearchRepo.findByProductNameContaining(keyword);
    }

    // --- HELPER TO SYNC DATA ---
    private void saveToElasticsearch(Product product, Category category, BigDecimal minPrice) {
        try {
            ProductDocument doc = new ProductDocument();
            doc.setId(product.getId());
            doc.setProductName(product.getProductName());
            doc.setDescription(product.getDescription());
            // Assuming getter is getCategoryName() based on Schema or standard convention.
            // If Schema.sql column is category_name, MyBatis mapping handles it to field 'categoryName' typically.
            doc.setCategoryName(category.getCategoryName());
            doc.setMinPrice(minPrice != null ? minPrice.doubleValue() : 0.0);

            productSearchRepo.save(doc);
        } catch (Exception e) {
            // Log error so transaction doesn't rollback due to search engine failure
            System.err.println("Error syncing product to Elasticsearch: " + e.getMessage());
        }
    }
}