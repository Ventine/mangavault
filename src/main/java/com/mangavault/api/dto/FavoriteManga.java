package com.mangavault.api.dto;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "favorite_mangas")
public record FavoriteManga(
    @Id 
    Long id, // Usaremos el ID de Jikan como nuestro ID propio
    String title,
    String imageUrl,
    Double score,
    String status,
    LocalDateTime addedAt
) {}