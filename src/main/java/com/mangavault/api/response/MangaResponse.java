package com.mangavault.api.response;

public record MangaResponse(
    Long id,
    String title,
    String synopsis,
    String imageUrl,
    Double score,
    Integer chapters
) {} 