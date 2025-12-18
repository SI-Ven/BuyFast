package com.example.buyfast.modules.product.service;

import com.example.buyfast.modules.category.model.Category;
import com.example.buyfast.modules.product.dto.CreateProductRequest;
// import com.example.buyfast.modules.product.dto.UpdateProductRequest;
import com.example.buyfast.modules.product.dto.ProductResponse;
import com.example.buyfast.modules.product.dto.UpdateProductRequest;
import com.example.buyfast.modules.product.model.Brand;
import com.example.buyfast.modules.product.model.Product;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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

    List<ProductResponse> searchProducts(String keyword);

    List<ProductResponse> searchProductsByImage(MultipartFile image);
    void saveToElasticsearch(Product product, Category category, BigDecimal minPrice);
    List<Brand> findAllBrand();

    // We will skip UpdateProduct for now as it's very complex
    // Product updateProduct(UUID productUuid, UpdateProductRequest request, UserDetails sellerDetails);
}