package com.example.buyfast.modules.product.search;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface ProductSearchRepo extends ElasticsearchRepository<ProductDocument, String> {

    // Fixes 500 error: Uses proper Match query for multi-word keywords
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"productName\", \"description\"]}}")
    List<ProductDocument> searchByNameOrDescription(String keyword);

    // Required to clean up old variant entries during product updates
    void deleteByProductId(Long productId);
}