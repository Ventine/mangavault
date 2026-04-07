package com.mangavault.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JikanMangaData(
    @JsonProperty("mal_id") Long malId,
    String title,
    String synopsis,
    String type,       // Agregado para el detalle
    String status,     // Agregado para el detalle
    Double score,
    Integer rank,      // Agregado para el detalle
    Integer chapters,
    JikanImages images,
    List<Genre> genres // Agregado para el detalle
) {
    // Sub-estructuras necesarias para mapear el JSON complejo de Jikan
    public record JikanImages(WebpImage webp) {
        public record WebpImage(@JsonProperty("image_url") String imageUrl) {}
    }
    
    public record Genre(String name) {}
    
    // NADA de métodos con restClient.get() aquí adentro.
}