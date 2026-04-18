package com.mangavault.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

record MangaEntry(
    @JsonProperty("mal_id") Long malId,
    String url,
    ImageWrapper images,
    String title
) {}