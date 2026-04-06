package com.mangavault.api.dto;

import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mangavault.api.response.JikanPaginationResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    public Optional<JikanMangaData> fetchMangaById(Long id) {
        try {
            var response = restClient.get()
                .uri("/manga/{id}/full", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    // Si la API externa da 404, devolvemos un opcional vacío más adelante
                    log.warn("Manga con ID {} no encontrado en Jikan", id);
                })
                .body(new ParameterizedTypeReference<JikanPaginationResponse<JikanMangaData>>() {});

            return Optional.ofNullable(response).map(JikanPaginationResponse::data);
        } catch (Exception e) {
            log.error("Error al obtener detalle del manga {}", id, e);
            return Optional.empty();
        }
    }
}