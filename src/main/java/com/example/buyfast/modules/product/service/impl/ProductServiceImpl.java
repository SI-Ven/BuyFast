package com.example.buyfast.modules.product.service.impl;

import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.repository.CategoryRepo;
import com.example.buyfast.modules.category.service.CategoryService;
import com.example.buyfast.modules.product.dto.*;
import com.example.buyfast.modules.product.model.*;
import com.example.buyfast.modules.product.repository.*;
import com.example.buyfast.modules.product.search.ProductDocument;
import com.example.buyfast.modules.product.search.ProductSearchRepo;
import com.example.buyfast.modules.product.service.ProductService;
import com.example.buyfast.modules.product.service.VectorEmbeddingService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final UuidService uuidService;

    // --- REPOS ---
    private final ProductOptionRepo productOptionRepo;
    private final ProductOptionValueRepo productOptionValueRepo;
    private final ProductVariantRepo productVariantRepo;
    private final ProductVariantValuesRepo productVariantValuesRepo;
    private final ProductImageRepo productImageRepo;
    private final BrandRepo brandRepo;
    private final UserRepo userRepo;

    // --- SEARCH & VECTOR SERVICES ---
    private final ProductSearchRepo productSearchRepo;
    private final VectorEmbeddingService vectorEmbeddingService;
    private final ElasticsearchOperations elasticsearchOperations;
    // 1. Add self-injection to fix @Async internal call problem
    private ProductServiceImpl self;

    @Autowired
    @Lazy
    public void setSelf(ProductServiceImpl self) {
        this.self = self;
    }
    // =================================================================
    // HELPER: Calculate Sale Price
    // =================================================================
    private BigDecimal calculateSalePrice(BigDecimal price, BigDecimal discountPercent) {
        if (price == null) return BigDecimal.ZERO; // Fix: Ensure price is never null
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }
        BigDecimal discountFactor = BigDecimal.ONE.subtract(
                discountPercent.divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
        );
        return price.multiply(discountFactor).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    // =================================================================
    // 1. CREATE PRODUCT (With Search Sync & Discount)
    // =================================================================
    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;

        // --- 1. BRAND LOGIC ---
        Brand brand;
        if (request.getNewBrandName() != null && !request.getNewBrandName().trim().isEmpty()) {
            brand = new Brand();
            brand.setBrandUuid(uuidService.generateUuid());
            brand.setBrandName(request.getNewBrandName());
            brandRepo.save(brand);
        } else {
            if (request.getBrandUuid() == null) {
                throw new IllegalArgumentException("Brand is required.");
            }
            brand = brandRepo.findByUuid(request.getBrandUuid())
                    .orElseThrow(() -> new IllegalArgumentException("Brand not found"));
        }

        // --- 2. Verify Category ---
        Category category = categoryRepo.findByCategoryUuid(request.getCategoryUuid())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // --- 3. Save Parent Product ---
        Product product = new Product();
        product.setProductUuid(uuidService.generateUuid());
        product.setProductName(request.getProductName());
        product.setSellerId(seller.getId());
        product.setCompanyId(seller.getCompanyId());
        product.setCategoryId(category.getId());
        product.setBrandId(brand.getId());
        product.setDescription(request.getDescription());
        product.setActive(true);
        productRepo.insert(product);

        List<ProductResponse.VariantResponse> variantResponses = new ArrayList<>();
        boolean mainImageSet = false;
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        Map<String, Set<String>> optionsSummary = new HashMap<>();

        // --- 4. Process Variants ---
        for (VariantRequest variantReq : request.getVariants()) {
            BigDecimal discount = variantReq.getDiscountPercentage() != null ? variantReq.getDiscountPercentage() : BigDecimal.ZERO;
            BigDecimal originalPrice = variantReq.getPrice();
            BigDecimal effectivePrice = calculateSalePrice(originalPrice, discount);

            if (minPrice == null || effectivePrice.compareTo(minPrice) < 0) minPrice = effectivePrice;
            if (maxPrice == null || effectivePrice.compareTo(maxPrice) > 0) maxPrice = effectivePrice;

            ProductVariant variant = new ProductVariant();
            variant.setVariantUuid(uuidService.generateUuid());
            variant.setProductId(product.getId());
            variant.setPrice(originalPrice);
            variant.setStockQuantity(variantReq.getStockQuantity());
            variant.setDiscountPercentage(discount);
            variant.setActive(true);

            String sku = request.getProductName().substring(0, Math.min(3, request.getProductName().length())).toUpperCase()
                    + "-" + variant.getVariantUuid().toString().substring(0, 8);
            variant.setSku(sku);

            productVariantRepo.insert(variant);

            List<ProductResponse.OptionResponse> optionResponses = new ArrayList<>();

            for (OptionValueRequest optionReq : variantReq.getOptions()) {
                optionsSummary.computeIfAbsent(optionReq.getOptionName(), k -> new HashSet<>())
                        .add(optionReq.getValueName());

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
                    .discountPercentage(variant.getDiscountPercentage())
                    .salePrice(effectivePrice)
                    .stockQuantity(variant.getStockQuantity())
                    .sku(variant.getSku())
                    .options(optionResponses)
                    .build());
        }

        // --- DYNAMIC SYNC: Index ALL images for better Visual Search UX ---
        self.saveToElasticsearch(product, category, minPrice);

        return ProductResponse.builder()
                .id(product.getId())
                .productUuid(product.getProductUuid())
                .productName(product.getProductName())
                .description(product.getDescription())
                .seller(ProductResponse.SellerInfo.builder()
                        .email(seller.getEmail())
                        .storeName("Official Store") // You can fetch this from the company table
                        .isVerified(true)
                        .build())
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .availableOptions(optionsSummary)
                .categoryId(product.getCategoryId())
                .isActive(product.isActive())
                .variants(variantResponses)
                .build();
    }

    // ... existing imports

    @Override
    public ProductResponse getMyProduct(UUID productUuid, UserDetails sellerDetails) {
        // 1. Fetch Product
        Product product = productRepo.findByUuid(productUuid)
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        // 2. Fetch Related Entities to eliminate hardcoded NULL fallbacks
        // Get subcategory and then use it to find the Main Category Name
        Category subCategory = categoryRepo.findById(product.getCategoryId()).orElse(null);
        String mainCatName = "Uncategorized";
        if (subCategory != null && subCategory.getMainCategoryId() != null) {
            // Fetch actual name from DB instead of hardcoded "General"
            mainCatName = categoryRepo.findMainCategoryNameById(subCategory.getMainCategoryId());
        }

        Brand brand = brandRepo.findById(product.getBrandId()).orElse(null);

        // Fetch actual Seller/User info
        User sellerUser = userRepo.findById(product.getSellerId()).orElse(null);

        // 3. Process Variants, Prices, and Options
        List<ProductVariant> variants = productVariantRepo.findAllByProductId(product.getId());
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        String firstImage = null;
        Map<String, Set<String>> optionsSummary = new HashMap<>();
        List<ProductResponse.VariantResponse> variantResponses = new ArrayList<>();

        for (ProductVariant variant : variants) {
            BigDecimal effectivePrice = calculateSalePrice(variant.getPrice(), variant.getDiscountPercentage());

            if (minPrice == null || effectivePrice.compareTo(minPrice) < 0) minPrice = effectivePrice;
            if (maxPrice == null || effectivePrice.compareTo(maxPrice) > 0) maxPrice = effectivePrice;

            List<ProductOptionValue> values = productVariantValuesRepo.findValuesByVariantId(variant.getId());
            List<ProductResponse.OptionResponse> optionResponses = new ArrayList<>();

            for (ProductOptionValue val : values) {
                ProductOption option = productOptionRepo.findById(val.getOptionId());
                List<ProductImage> images = productImageRepo.findAllByOptionValueId(val.getId());
                List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());

                // Pick the first available image as the main hero image
                if (firstImage == null && !imageUrls.isEmpty()) firstImage = imageUrls.get(0);

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
                    .discountPercentage(variant.getDiscountPercentage())
                    .salePrice(effectivePrice)
                    .stockQuantity(variant.getStockQuantity())
                    .sku(variant.getSku())
                    .options(optionResponses)
                    .build());
        }

        // 4. Build Professional Response (Alibaba/Amazon Style)
        return ProductResponse.builder()
                .id(product.getId())
                .productUuid(product.getProductUuid())
                .productName(product.getProductName())
                .brandName(brand != null ? brand.getBrandName() : "Generic")
                .description(product.getDescription())
                .minPrice(minPrice != null ? minPrice : BigDecimal.ZERO)
                .maxPrice(maxPrice != null ? maxPrice : BigDecimal.ZERO)
                .mainImage(firstImage != null ? firstImage : "")
                .categoryName(subCategory != null ? subCategory.getCategoryName() : "Other")
                .mainCategoryName(mainCatName) // Now dynamic from DB
                .seller(ProductResponse.SellerInfo.builder()
                        .storeName(sellerUser != null ? sellerUser.getUsername() + "'s Store" : "Official Store")
                        .email(sellerUser != null ? sellerUser.getEmail() : "")
                        .isVerified(true)
                        .build())
                .averageRating(0.0)
                .totalReviews(0)
                .availableOptions(optionsSummary)
                .categoryId(product.getCategoryId())
                .isActive(product.isActive())
                .variants(variantResponses)
                .build();
    }
    @Override
    @Transactional
    public ProductResponse updateProduct(UUID productUuid, UpdateProductRequest request, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;
        Product product = productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        if (request.getProductName() != null) product.setProductName(request.getProductName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getIsActive() != null) product.setActive(request.getIsActive());

        if (request.getCategoryUuid() != null) {
            Category category = categoryRepo.findByCategoryUuid(request.getCategoryUuid())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategoryId(category.getId());
        }

        productRepo.update(product);

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            productImageRepo.deleteAllByProductId(product.getId());
            productVariantRepo.deleteAllByProductId(product.getId());
            productOptionRepo.deleteAllByProductId(product.getId());
            processVariants(product, request.getVariants());
        }

        Category category = categoryRepo.findById(product.getCategoryId()).orElse(new Category());

        List<ProductVariant> currentVariants = productVariantRepo.findAllByProductId(product.getId());
        BigDecimal minPrice = currentVariants.stream()
                .map(v -> calculateSalePrice(v.getPrice(), v.getDiscountPercentage()))
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        // SYNC UPDATED PRICE TO HOME PAGE INDEX
        self.saveToElasticsearch(product, category, minPrice);

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
            BigDecimal discount = variantReq.getDiscountPercentage() != null ? variantReq.getDiscountPercentage() : BigDecimal.ZERO;
            variant.setDiscountPercentage(discount);
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

    @Override
    @Transactional
    public void deleteProduct(UUID productUuid, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;
        Product product = productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found or permission denied."));

        productRepo.deleteByUuidAndSellerId(product.getProductUuid(), seller.getId());

        try {
            productSearchRepo.deleteById(String.valueOf(product.getId()));
        } catch (Exception e) {
            log.error("Warning: Failed to delete product from Elasticsearch: {}", e.getMessage());
        }
    }

    // In ProductServiceImpl.java - getAllProductsForHome
// src/main/java/com/example/buyfast/modules/product/service/impl/ProductServiceImpl.java

    @Override
    public List<ProductResponse> getAllProductsForHome(int page, int size) {
        int offset = (page - 1) * size;
        // 1. Get the summary list (fast query)
        List<ProductResponse> products = productRepo.findAllActiveProductsSummary(size, offset);

        // 2. Populate Prices and Variants for the "Card" display
        for (ProductResponse p : products) {
            List<ProductVariant> variants = productVariantRepo.findAllByProductId(p.getId());

            BigDecimal min = null;
            BigDecimal max = null;
            List<ProductResponse.VariantResponse> variantList = new ArrayList<>();

            for (ProductVariant v : variants) {
                // Use calculateSalePrice to handle discounts
                BigDecimal salePrice = calculateSalePrice(v.getPrice(), v.getDiscountPercentage());

                if (min == null || salePrice.compareTo(min) < 0) min = salePrice;
                if (max == null || salePrice.compareTo(max) > 0) max = salePrice;

//                variantList.add(ProductResponse.VariantResponse.builder()
//                        .variantUuid(v.getVariantUuid())
//                        .price(v.getPrice())
//                        .discountPercentage(v.getDiscountPercentage())
//                        .salePrice(salePrice)
//                        .stockQuantity(v.getStockQuantity())
//                        .build());
            }

//            // Standardize values to avoid NULL on frontend
//            p.setVariants(variantList);
            p.setMinPrice(min != null ? min : BigDecimal.ZERO);
            p.setMaxPrice(max != null ? max : BigDecimal.ZERO);
            p.setAverageRating(0.0); // Default for card display
            p.setTotalReviews(0);

            // If main image is missing, try to take it from a variant
            if (p.getMainImage() == null || p.getMainImage().isEmpty()) {
                p.setMainImage(""); // Keep it empty string instead of null
            }
        }
        return products;
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        // 1. Search Elasticsearch using the new Match query (Fixes 500 error)
        List<ProductDocument> docs = productSearchRepo.searchByNameOrDescription(keyword);

        if (docs.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Extract Product IDs (Long) instead of Document IDs (String)
        // Fixes incompatible types: java.lang.Long cannot be converted to java.lang.String
        Set<Long> productIds = docs.stream()
                .map(ProductDocument::getProductId) // Use the Long productId field
                .collect(Collectors.toSet());

        // 3. Fetch clean "Card" response from DB
        return productRepo.findAllSummaryByIds(new ArrayList<>(productIds));
    }
    @Override
    public List<ProductResponse> searchProductsByImage(MultipartFile image) {
        List<Double> searchVector = vectorEmbeddingService.getVectorFromFile(image);
        if (searchVector == null || searchVector.isEmpty()) return Collections.emptyList();

        String script = "if (doc['imageVector'].size() == 0) { return 0; } return cosineSimilarity(params.query_vector, 'imageVector') + 1.0;";
        String querySource = "{\"script_score\": {\"query\": {\"match_all\": {}}, \"script\": {\"source\": \"" + script + "\", \"params\": {\"query_vector\": " + searchVector + "}}}}";

        StringQuery query = new StringQuery(querySource);
        query.setPageable(PageRequest.of(0, 20)); // Fetch more to allow for unique filtering

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        if (hits.isEmpty()) return Collections.emptyList();

        // --- FIX: Use LinkedHashMap to keep the order of relevance but group by Product ID ---
        Map<Long, ProductResponse> uniqueBestMatches = new LinkedHashMap<>();

        for (SearchHit<ProductDocument> hit : hits) {
            ProductDocument doc = hit.getContent();
            Long productId = doc.getProductId();

            // If this product isn't in our map yet, it's the "Best Match" for this specific product
            if (!uniqueBestMatches.containsKey(productId)) {
                ProductResponse res = productRepo.findSummaryById(productId);
                if (res != null) {
                    // Overwrite generic main image with the specific variant image that matched
                    res.setMainImage(doc.getImageUrl());
                    uniqueBestMatches.put(productId, res);
                }
            }

            // Stop once we have 10 unique product matches
            if (uniqueBestMatches.size() >= 10) break;
        }

        return new ArrayList<>(uniqueBestMatches.values());
    }
    @Override
    public List<Brand> findAllBrand() {
        return brandRepo.findAll();
    }

    @Override
    public ProductResponse getProductDetailsPublic(UUID productUuid) {
        // 1. Fetch the product and ensure it is active
        Product product = productRepo.findByUuid(productUuid)
                .filter(Product::isActive)
                .orElseThrow(() -> new IllegalStateException("Product not found or is currently unavailable."));

        // 2. Resolve dynamic Category names (Alibaba Style)
        Category subCategory = categoryRepo.findById(product.getCategoryId()).orElse(null);
        String mainCatName = "Uncategorized";
        if (subCategory != null && subCategory.getMainCategoryId() != null) {
            mainCatName = categoryRepo.findMainCategoryNameById(subCategory.getMainCategoryId());
        }

        // 3. Fetch Brand and Seller Information
        Brand brand = brandRepo.findById(product.getBrandId()).orElse(null);
        User sellerUser = userRepo.findById(product.getSellerId()).orElse(null);

        // 4. Declare variables for variants, prices, and options summary
        List<ProductVariant> variants = productVariantRepo.findAllByProductId(product.getId());
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        String firstImage = null;
        Map<String, Set<String>> optionsSummary = new HashMap<>();
        List<ProductResponse.VariantResponse> variantResponses = new ArrayList<>();

        // 5. Process Variants, Prices, and Options (Crucial logic to fix the "symbol not found" errors)
        for (ProductVariant variant : variants) {
            BigDecimal effectivePrice = calculateSalePrice(variant.getPrice(), variant.getDiscountPercentage());

            if (minPrice == null || effectivePrice.compareTo(minPrice) < 0) minPrice = effectivePrice;
            if (maxPrice == null || effectivePrice.compareTo(maxPrice) > 0) maxPrice = effectivePrice;

            List<ProductOptionValue> values = productVariantValuesRepo.findValuesByVariantId(variant.getId());
            List<ProductResponse.OptionResponse> optionResponses = new ArrayList<>();

            for (ProductOptionValue val : values) {
                ProductOption option = productOptionRepo.findById(val.getOptionId());
                List<ProductImage> images = productImageRepo.findAllByOptionValueId(val.getId());
                List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());

                // Pick the first available image as the main hero image
                if (firstImage == null && !imageUrls.isEmpty()) firstImage = imageUrls.get(0);

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
                    .discountPercentage(variant.getDiscountPercentage())
                    .salePrice(effectivePrice)
                    .stockQuantity(variant.getStockQuantity())
                    .sku(variant.getSku())
                    .options(optionResponses)
                    .build());
        }

        // 6. Build the Response with No Nulls
        return ProductResponse.builder()
                .id(product.getId())
                .productUuid(product.getProductUuid())
                .productName(product.getProductName())
                .brandName(brand != null ? brand.getBrandName() : "Generic")
                .description(product.getDescription())
                .minPrice(minPrice != null ? minPrice : BigDecimal.ZERO)
                .maxPrice(maxPrice != null ? maxPrice : BigDecimal.ZERO)
                .mainImage(firstImage != null ? firstImage : "")
                .categoryName(subCategory != null ? subCategory.getCategoryName() : "Other")
                .mainCategoryName(mainCatName)
                .seller(ProductResponse.SellerInfo.builder()
                        .storeName(sellerUser != null ? sellerUser.getUsername() + "'s Store" : "Official Store")
                        .email(sellerUser != null ? sellerUser.getEmail() : "")
                        .isVerified(true)
                        .build())
                .averageRating(0.0)
                .totalReviews(0)
                .availableOptions(optionsSummary)
                .isActive(product.isActive())
                .variants(variantResponses)
                .build();
    }

    // --- HELPER TO SYNC DATA: Fixed to index EVERY product image ---
    @Async("taskExecutor") // Matches the bean name in AsyncConfig
    @Override // Ensure it's in the interface or public
    public void saveToElasticsearch(Product product, Category category, BigDecimal minPrice) {
        try {
            log.info("🚀 Background indexing started for Product ID: {}", product.getId());

            // Delete old entries to prevent duplicates
            productSearchRepo.deleteByProductId(product.getId());

            List<ProductImage> allImages = productImageRepo.findAllByProductId(product.getId());

            for (ProductImage img : allImages) {
                ProductDocument doc = new ProductDocument();
                doc.setId(product.getId() + "_" + img.getId());
                doc.setProductId(product.getId());
                doc.setProductName(product.getProductName());
                doc.setDescription(product.getDescription());
                doc.setCategoryName(category.getCategoryName());
                doc.setMinPrice(minPrice != null ? minPrice.doubleValue() : 0.0);
                doc.setImageUrl(img.getImageUrl());

                // THE SLOW PART: Now happens in the background
                List<Double> vector = vectorEmbeddingService.getVectorFromUrl(img.getImageUrl());

                if (vector != null && !vector.isEmpty()) {
                    doc.setImageVector(vector);
                    productSearchRepo.save(doc);
                }
            }
            log.info("✅ Background indexing finished for Product ID: {}", product.getId());
        } catch (Exception e) {
            log.error("❌ Background Sync Error for Product {}: ", product.getId(), e);
        }
    }

}