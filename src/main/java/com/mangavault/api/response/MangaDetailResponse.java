package com.mangavault.api.response;

import java.util.List;

public record MangaDetailResponse(
    Long id,
    String title,
    String synopsis,
    String type,
    String status,
    Double score,
    Integer rank,
    List<String> genres,
    String imageUrl
) {}
