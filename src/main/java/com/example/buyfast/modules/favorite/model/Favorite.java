package com.example.buyfast.modules.favorite.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Favorite {
    private Long id;
    private UUID favoriteUuid;
    private Long userId;
    private Long productId;
    private Timestamp createdAt;
}
