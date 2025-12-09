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
import lombok.extern.slf4j.Slf4j; // 1. Added Logging
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

@Slf4j // 2. Enable Logging
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

    // --- SEARCH & VECTOR SERVICES ---
    private final ProductSearchRepo productSearchRepo;
    private final VectorEmbeddingService vectorEmbeddingService;
    private final ElasticsearchOperations elasticsearchOperations;

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
        product.setActive(true);
        productRepo.insert(product);

        // --- PREPARE VARIABLES FOR RESPONSE & HINTS ---
        List<ProductResponse.VariantResponse> variantResponses = new ArrayList<>();
        boolean mainImageSet = false;

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        Map<String, Set<String>> optionsSummary = new HashMap<>();
        // ----------------------------------------------

        // 3. Process Variants
        for (VariantRequest variantReq : request.getVariants()) {

            // --- Price Calculation ---
            BigDecimal price = variantReq.getPrice();
            if (minPrice == null || price.compareTo(minPrice) < 0) minPrice = price;
            if (maxPrice == null || price.compareTo(maxPrice) > 0) maxPrice = price;

            ProductVariant variant = new ProductVariant();
            variant.setVariantUuid(uuidService.generateUuid());
            variant.setProductId(product.getId());
            variant.setPrice(variantReq.getPrice());
            variant.setStockQuantity(variantReq.getStockQuantity());
            variant.setActive(true);

            String sku = request.getProductName().substring(0, Math.min(3, request.getProductName().length())).toUpperCase()
                    + "-" + variant.getVariantUuid().toString().substring(0, 8);
            variant.setSku(sku);

            productVariantRepo.insert(variant);

            List<ProductResponse.OptionResponse> optionResponses = new ArrayList<>();

            // 4. Process Options
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

                            // Ensure logic for main image
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
        // This MUST be called after images are inserted into DB
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
                .sellerId(seller.getEmail())
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .availableOptions(optionsSummary)
                .categoryId(product.getCategoryId())
                .isActive(product.isActive())
                .variants(variantResponses)
                .build();
    }

    // =================================================================
    // 2. UPDATE PRODUCT
    // =================================================================
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
                .map(ProductVariant::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

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
            productSearchRepo.deleteById(product.getId());
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

    // =================================================================
    // 4. SEARCH METHODS
    // =================================================================
    @Override
    public List<ProductDocument> searchProducts(String keyword) {
        return productSearchRepo.findByProductNameContaining(keyword);
    }

    @Override
    public List<ProductDocument> searchProductsByImage(MultipartFile image) {
        List<Double> searchVector = vectorEmbeddingService.getVectorFromFile(image);

        if (searchVector == null || searchVector.isEmpty()) {
            return Collections.emptyList();
        }

        String script = "cosineSimilarity(params.query_vector, 'imageVector') + 1.0";
        String querySource = "{" +
                "  \"script_score\": {" +
                "    \"query\": {\"match_all\": {}}," +
                "    \"script\": {" +
                "      \"source\": \"" + script + "\"," +
                "      \"params\": {" +
                "        \"query_vector\": " + searchVector.toString() +
                "      }" +
                "    }" +
                "  }" +
                "}";

        StringQuery query = new StringQuery(querySource);
        query.setPageable(PageRequest.of(0, 10));

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        return hits.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }

    // --- HELPER TO SYNC DATA ---
    private void saveToElasticsearch(Product product, Category category, BigDecimal minPrice) {
        try {
            ProductDocument doc = new ProductDocument();
            doc.setId(product.getId());
            doc.setProductName(product.getProductName());
            doc.setDescription(product.getDescription());
            doc.setCategoryName(category.getCategoryName());
            doc.setMinPrice(minPrice != null ? minPrice.doubleValue() : 0.0);

            // Fetch Main Image
            Optional<ProductImage> mainImageOpt = productImageRepo.findFirstByProductIdAndIsMainTrue(product.getId());

            if (mainImageOpt.isPresent()) {
                String imageUrl = mainImageOpt.get().getImageUrl();
                log.info("Found main image for vectorization: {}", imageUrl); // Debug Log

                List<Double> vector = vectorEmbeddingService.getVectorFromUrl(imageUrl);

                if (vector != null && !vector.isEmpty()) {
                    doc.setImageVector(vector);
                    log.info("Vector generated successfully for Product ID: {}", product.getId());
                } else {
                    log.warn("Vector service returned NULL/Empty for Product ID: {}", product.getId());
                }
            } else {
                log.warn("No main image found for Product ID: {} - Skipping vectorization", product.getId());
            }

            productSearchRepo.save(doc);

        } catch (Exception e) {
            log.error("Error syncing product to Elasticsearch: ", e);
        }
    }
}