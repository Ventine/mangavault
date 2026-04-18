package com.mangavault.api.dto;

record RecommendationData(
    MangaEntry entry,
    String url,
    Integer votes
) {}