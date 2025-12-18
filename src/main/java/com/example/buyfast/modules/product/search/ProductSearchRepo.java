package com.example.buyfast.modules.product.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface ProductSearchRepo extends ElasticsearchRepository<ProductDocument, String> {
    // Standard keyword search
    List<ProductDocument> findByProductNameContaining(String productName);

    // Required to clean up old variant image entries during updates
    void deleteByProductId(Long productId);
}