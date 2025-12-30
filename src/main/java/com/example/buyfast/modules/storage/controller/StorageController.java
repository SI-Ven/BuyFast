package com.example.buyfast.modules.storage.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.storage.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    // 1. Endpoint for Single File Upload (Good for Profile Pictures, etc.)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a single file")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @Parameter(
                    description = "Select file to upload",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("file") MultipartFile file) {

        String fileUrl = storageService.uploadFile(file);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", fileUrl));
    }

    // 2. Endpoint for Multiple File Uploads (Moved from ProductController)
    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple files")
    public ResponseEntity<ApiResponse<List<String>>> uploadFiles(
            @Parameter(
                    description = "Select images to upload",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("files") List<MultipartFile> files) {

        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String url = storageService.uploadFile(file);
                fileUrls.add(url);
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Files uploaded successfully", fileUrls));
    }
}