package com.example.buyfast.modules.product.service.Impl;

import com.example.buyfast.modules.category.repository.CategoryRepo;
import com.example.buyfast.modules.product.dto.CreateProductRequest;
import com.example.buyfast.modules.product.dto.OptionValueRequest;
import com.example.buyfast.modules.product.dto.VariantRequest;
// import com.example.buyfast.modules.product.dto.UpdateProductRequest;
import com.example.buyfast.modules.product.model.Product;
import com.example.buyfast.modules.product.model.ProductOption;
import com.example.buyfast.modules.product.model.ProductOptionValue;
import com.example.buyfast.modules.product.model.ProductVariant;
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

    @Override
    @Transactional
    public Product createProduct(CreateProductRequest request, UserDetails sellerDetails) {
        User seller = (User) sellerDetails;

        // 1. Find Category
        Long categoryId = categoryRepo.findCategoryIdByUuid(request.getCategoryUuid());
        if (categoryId == null) {
            throw new IllegalStateException("Category not found with UUID: " + request.getCategoryUuid());
        }

        // 2. Create Base Product
        Product product = new Product();
        product.setProductUuid(uuidService.generateUuid());
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setCategoryId(categoryId);
        product.setActive(true);
        product.setSellerId(seller.getId());
        product.setCompanyId(seller.getCompanyId());

        productRepo.insert(product);
        Long productId = product.getId(); // Get the new internal product ID

        // --- 3. Create Options, Values, and Variants ---

        // Maps to prevent creating duplicate options/values
        Map<String, ProductOption> optionsMap = new HashMap<>();
        Map<String, ProductOptionValue> valuesMap = new HashMap<>();

        for (VariantRequest variantDto : request.getVariants()) {

            // a. Create the Variant (SKU)
            ProductVariant variant = new ProductVariant();
            variant.setVariantUuid(uuidService.generateUuid());
            variant.setProductId(productId);
            variant.setPrice(variantDto.getPrice());
            variant.setStockQuantity(variantDto.getStockQuantity());
            variant.setActive(true);
            // We could generate a SKU here, e.g., "PROD-1001-SM-RED"

            productVariantRepo.insert(variant);
            Long variantId = variant.getId(); // Get the new internal variant ID

            List<Long> valueIdsToLink = new ArrayList<>();

            // b. Loop through the options for this variant (e.g., "Size: Small", "Color: Red")
            for (OptionValueRequest optionValueDto : variantDto.getOptions()) {

                // c. Get or Create the "Option" (e.g., "Size")
                ProductOption option = optionsMap.computeIfAbsent(optionValueDto.getOptionName(), optionName -> {
                    ProductOption newOption = new ProductOption();
                    newOption.setOptionUuid(uuidService.generateUuid());
                    newOption.setProductId(productId);
                    newOption.setOptionName(optionName);
                    productOptionRepo.insert(newOption);
                    return newOption;
                });

                // d. Get or Create the "Value" (e.g., "Small")
                String valueKey = option.getId() + ":" + optionValueDto.getValueName();
                ProductOptionValue value = valuesMap.computeIfAbsent(valueKey, v -> {
                    ProductOptionValue newValue = new ProductOptionValue();
                    newValue.setValueUuid(uuidService.generateUuid());
                    newValue.setOptionId(option.getId());
                    newValue.setValueName(optionValueDto.getValueName());
                    productOptionValueRepo.insert(newValue);
                    return newValue;
                });

                valueIdsToLink.add(value.getId());
            }

            // e. Link the Variant to its OptionValues
            if (!valueIdsToLink.isEmpty()) {
                productVariantValuesRepo.linkVariantToValues(variantId, valueIdsToLink);
            }
        }

        return product;
    }

    /*
     * Note: updateProduct is very complex and would require logic to add new variants,
     * update existing ones, and delete old ones. We will skip it for now.
     */

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