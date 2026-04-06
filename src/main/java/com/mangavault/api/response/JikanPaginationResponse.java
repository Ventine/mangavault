package com.mangavault.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JikanPaginationResponse<T>(
    @JsonProperty("data") List<T> data
) {}