package com.example.buyfast.modules.product.search;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String productName;

    @Field(type = FieldType.Text, analyzer = "english")
    private String description;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Double)
    private Double minPrice;

    // --- NEW VECTOR FIELD ---
    // dims=512 is standard for CLIP models. Adjust if your model is different.
    @Field(type = FieldType.Dense_Vector, dims = 512, index = true)
    private List<Double> imageVector;
}