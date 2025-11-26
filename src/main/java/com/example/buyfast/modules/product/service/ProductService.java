package com.example.buyfast.modules.product.service;

import com.example.buyfast.modules.product.dto.CreateProductRequest;
// import com.example.buyfast.modules.product.dto.UpdateProductRequest;
import com.example.buyfast.modules.product.dto.ProductResponse;
import com.example.buyfast.modules.product.dto.UpdateProductRequest;
import com.example.buyfast.modules.product.model.Product;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    /**
     * Creates a new product and all its variants for the currently authenticated seller.
     */
    ProductResponse createProduct(CreateProductRequest request, UserDetails sellerDetails);

    /**
     * Deletes a product owned by the currently authenticated seller.
     */
    void deleteProduct(UUID productUuid, UserDetails sellerDetails);

   

    ProductResponse updateProduct(UUID productUuid, UpdateProductRequest request, UserDetails sellerDetails);

    ProductResponse getMyProduct(UUID productUuid, UserDetails sellerDetails);
    List<ProductResponse> getAllProductsForHome(int page, int size);

    // We will skip UpdateProduct for now as it's very complex
    // Product updateProduct(UUID productUuid, UpdateProductRequest request, UserDetails sellerDetails);
}