package com.example.buyfast.modules.favorite.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.favorite.model.Favorite;
import com.example.buyfast.modules.favorite.service.FavoriteService;
import com.example.buyfast.modules.product.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping("/{productUuid}")
    public ResponseEntity<ApiResponse<Favorite>> addFavorite(
            @PathVariable UUID productUuid,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        Favorite favorites = favoriteService.addFavorite(productUuid,userDetails);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Add Favorite Successfully", favorites));
    }

    @GetMapping
    @Operation(summary = "Get my favorite products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMyFavorites(@AuthenticationPrincipal UserDetails userDetails) {
        List<ProductResponse> favorites = favoriteService.getMyFavorites(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Favorites retrieved successfully.", favorites));
    }

    @DeleteMapping("/{productUuid}")
    @Operation(summary = "delete favorite product")
    public ResponseEntity<ApiResponse<String>> deleteFavorite(
            @PathVariable UUID productUuid, @AuthenticationPrincipal UserDetails userDetails) {

        favoriteService.deleteFavorite(productUuid,userDetails);
        return ResponseEntity.ok(ApiResponse.success("Delete Favorite Successfully",null));
    }
}
