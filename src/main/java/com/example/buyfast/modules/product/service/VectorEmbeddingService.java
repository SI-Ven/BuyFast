package com.example.buyfast.modules.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VectorEmbeddingService {

    private final RestTemplate restTemplate;

    @Value("${fastapi.url:http://localhost:8000}")
    private String fastApiUrl;

    public List<Double> getVectorFromUrl(String imageUrl) {
        try {
            String url = fastApiUrl + "/vectorize/url";
            Map<String, String> body = Collections.singletonMap("url", imageUrl);
            // Assuming FastAPI returns a JSON list of doubles
            return restTemplate.postForObject(url, body, List.class);
        } catch (Exception e) {
            System.err.println("Warning: Failed to vectorize image URL: " + e.getMessage());
            return null;
        }
    }

    public List<Double> getVectorFromFile(MultipartFile file) {
        try {
            String url = fastApiUrl + "/vectorize/file";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            return restTemplate.postForObject(url, requestEntity, List.class);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file for vectorization", e);
        }
    }
}