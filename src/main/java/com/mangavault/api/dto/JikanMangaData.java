package com.mangavault.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JikanMangaData(
    @JsonProperty("mal_id") Long malId,
    String title,
    String synopsis,
    Double score,
    Integer chapters,
    JikanImages images
) {
    public record JikanImages(WebpImage webp) {
        public record WebpImage(@JsonProperty("image_url") String imageUrl) {}
    }
}