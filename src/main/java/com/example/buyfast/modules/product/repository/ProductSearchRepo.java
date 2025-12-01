package com.example.buyfast.modules.product.repository;

import com.example.buyfast.modules.product.search.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface ProductSearchRepo extends ElasticsearchRepository<ProductDocument, Long> {
    List<ProductDocument> findByProductNameContaining(String productName);
    List<ProductDocument> findByDescriptionContaining(String description);
}