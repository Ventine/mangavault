package com.mangavault.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaginationMetadata(
    @JsonProperty("last_visible_page") int lastVisiblePage,
    @JsonProperty("has_next_page") boolean hasNextPage
) {}