package com.example.buyfast.modules.favorite.service;

import com.example.buyfast.modules.favorite.model.Favorite;
import com.example.buyfast.modules.product.dto.ProductResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface FavoriteService {

    Favorite addFavorite(UUID productUuid, UserDetails userDetails);

    List<ProductResponse> getMyFavorites(UserDetails userDetails);
}
