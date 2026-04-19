package com.mangavault.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mangavault.api.response.MangaResponse;

@JsonIgnoreProperties(ignoreUnknown = true) 
public record JikanTopWrapper(
    @JsonProperty("data") List<MangaResponse> data, // Asegúrate de que se llame 'data'
    @JsonProperty("pagination") PaginationMetadata pagination
) {}