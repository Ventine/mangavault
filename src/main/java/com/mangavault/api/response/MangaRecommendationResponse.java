package com.mangavault.api.response;

public record MangaRecommendationResponse(
    Long id,
    String title,
    String imageUrl,
    String url,
    Integer votes // Jikan devuelve cuánta gente recomendó esto
) {}