package com.example.buyfast.modules.favorite.service.impl;

import com.example.buyfast.modules.favorite.model.Favorite;
import com.example.buyfast.modules.favorite.repository.FavoriteRepo;
import com.example.buyfast.modules.favorite.service.FavoriteService;
import com.example.buyfast.modules.product.dto.ProductResponse;
import com.example.buyfast.modules.product.model.Product;
import com.example.buyfast.modules.product.repository.ProductRepo;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepo favoriteRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    @Override
    @Transactional
    public Favorite addFavorite(UUID productUuid, UserDetails userDetails) {
        User user = getUser(userDetails);
        Product product = getProduct(productUuid);
        if(favoriteRepo.existsByUserAndProduct(user.getId(),product.getId())){
            throw new IllegalArgumentException("Favorite already exists");

        }
        Favorite favorite = new Favorite();
        favorite.setFavoriteUuid(UUID.randomUUID());
        favorite.setUserId(user.getId());
        favorite.setProductId(product.getId());

       return favoriteRepo.save(favorite);
    }

    @Override
    public List<ProductResponse> getMyFavorites(UserDetails userDetails) {
        User user = getUser(userDetails);
        return favoriteRepo.findFavoritesByUserId(user.getId());
    }


    private User getUser(UserDetails userDetails) {
        return userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Product getProduct(UUID productUuid) {
        return productRepo.findByUuid(productUuid)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }
}
