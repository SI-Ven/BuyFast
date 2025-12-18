package com.example.buyfast.modules.product.service.impl;

import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.category.repository.CategoryRepo;
import com.example.buyfast.modules.product.dto.*;
import com.example.buyfast.modules.product.model.*;
import com.example.buyfast.modules.product.repository.*;
import com.example.buyfast.modules.product.search.ProductDocument;
import com.example.buyfast.modules.product.search.ProductSearchRepo;
import com.example.buyfast.modules.product.service.ProductService;
import com.example.buyfast.modules.product.service.VectorEmbeddingService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
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

    // --- SEARCH & VECTOR SERVICES ---
    private final ProductSearchRepo productSearchRepo;
    private final VectorEmbeddingService vectorEmbeddingService;
    private final ElasticsearchOperations elasticsearchOperations;

    // =================================================================
    // HELPER: Calculate Sale Price
    // =================================================================
    private BigDecimal calculateSalePrice(BigDecimal price, BigDecimal discountPercent) {
        if (price == null) return BigDecimal.ZERO;
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }
        BigDecimal discountFactor = BigDecimal.ONE.subtract(discountPercent.divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
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
        saveToElasticsearch(product, category, minPrice);

        return ProductResponse.builder()
                .id(product.getId())
                .productUuid(product.getProductUuid())
                .productName(product.getProductName())
                .description(product.getDescription())
                .sellerId(seller.getEmail())
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
        Product product = productRepo.findByUuidAndSellerId(productUuid, seller.getId())
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        List<ProductVariant> variants = productVariantRepo.findAllByProductId(product.getId());
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
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

        return ProductResponse.builder()
                .id(product.getId())
                .productUuid(product.getProductUuid())
                .productName(product.getProductName())
                .description(product.getDescription())
                .sellerId(seller.getEmail())
                .minPrice(minPrice)
                .maxPrice(maxPrice)
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
        saveToElasticsearch(product, category, minPrice);

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

    @Override
    public List<ProductResponse> getAllProductsForHome(int page, int size) {
        if (page < 1) page = 1;
        int offset = (page - 1) * size;
        return productRepo.findAllActiveProductsSummary(size, offset);
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        // 1. Get documents from Elasticsearch (Fast)
        List<ProductDocument> docs = productSearchRepo.findByProductNameContaining(keyword);

        if (docs.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. FIX: Extract the productId (Long) instead of the document id (String)
        // We use a Set to handle products with multiple matching images
        Set<Long> productIds = docs.stream()
                .map(ProductDocument::getProductId) // This is already Long
                .collect(Collectors.toSet());

        // 3. Fetch full "Card" details from PostgreSQL (Clean & Fast)
        return productRepo.findAllSummaryByIds(new ArrayList<>(productIds));
    }
    @Override
    public List<ProductResponse> searchProductsByImage(MultipartFile image) {
        List<Double> searchVector = vectorEmbeddingService.getVectorFromFile(image);
        if (searchVector == null || searchVector.isEmpty()) return Collections.emptyList();

        String script = "if (doc['imageVector'].size() == 0) { return 0; } return cosineSimilarity(params.query_vector, 'imageVector') + 1.0;";
        String querySource = "{\"script_score\": {\"query\": {\"match_all\": {}}, \"script\": {\"source\": \"" + script + "\", \"params\": {\"query_vector\": " + searchVector + "}}}}";

        StringQuery query = new StringQuery(querySource);
        query.setPageable(PageRequest.of(0, 10));

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        if (hits.isEmpty()) return Collections.emptyList();

        List<ProductResponse> responses = new ArrayList<>();
        for (SearchHit<ProductDocument> hit : hits) {
            ProductDocument doc = hit.getContent();
            ProductResponse res = productRepo.findSummaryById(doc.getProductId());
            if (res != null) {
                res.setMainImage(doc.getImageUrl()); // Swap to matched image
                responses.add(res);
            }
        }
        return responses;
    }
    @Override
    public List<Brand> findAllBrand() {
        return brandRepo.findAll();
    }

    // --- HELPER TO SYNC DATA: Fixed to index EVERY product image ---
    private void saveToElasticsearch(Product product, Category category, BigDecimal minPrice) {
        try {
            productSearchRepo.deleteByProductId(product.getId()); // Clean old image entries
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

                List<Double> vector = vectorEmbeddingService.getVectorFromUrl(img.getImageUrl());
                if (vector != null && !vector.isEmpty()) {
                    doc.setImageVector(vector);
                    productSearchRepo.save(doc);
                }
            }
        } catch (Exception e) {
            log.error("Error syncing to Elasticsearch: ", e);
        }
    }
}