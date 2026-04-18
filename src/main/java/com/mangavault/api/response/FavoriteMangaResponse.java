package com.mangavault.api.response;

import java.time.LocalDateTime;

public record FavoriteMangaResponse(
    Long id,
    String title,
    String imageUrl,
    Double score,
    String status,
    LocalDateTime addedAt
) {}