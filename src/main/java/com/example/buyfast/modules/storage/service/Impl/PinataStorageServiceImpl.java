package com.example.buyfast.modules.storage.service.Impl;

import com.example.buyfast.modules.storage.dto.PinataResponse;
import com.example.buyfast.modules.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PinataStorageServiceImpl implements StorageService {

    private final RestTemplate restTemplate;

    @Value("${pinata.jwt}")
    private String pinataJwt;

    @Value("${pinata.gateway.url}")
    private String pinataGatewayUrl;

    private final String PINATA_API_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";

    @Override
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot upload an empty file.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + pinataJwt);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<PinataResponse> response = restTemplate.postForEntity(
                    PINATA_API_URL,
                    requestEntity,
                    PinataResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String ipfsHash = response.getBody().getIpfsHash();
                return pinataGatewayUrl + ipfsHash;
            } else {
                throw new RuntimeException("Failed to upload file to Pinata. Status: " + response.getStatusCode());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read file bytes.", e);
        }
    }
}