package com.mangavault.api.dto; 

import com.mangavault.api.response.JikanPaginationResponse;
import com.mangavault.api.response.JikanSingleResponse; // IMPORTANTE: Debes tener este record creado
import com.mangavault.api.response.MangaRecommendationResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

import com.mangavault.api.error.MangaExternalApiException;

@Component
@Slf4j // Agregamos Slf4j aquí, donde sí corresponde hacer logs
public class JikanGateway {

    private final RestClient restClient;
    private  String baseUrl="";

    public JikanGateway(
        RestClient.Builder builder, 
        @Value("${app.external-api.jikan.base-url:https://api.jikan.moe/v4}") String baseUrl) {
    
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    // 1. MÉTODO DE BÚSQUEDA (El que ya tenías)
    public List<JikanMangaData> fetchMangasFromExternalApi(String query) {
        var response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/manga")
                        .queryParam("q", query)
                        .queryParam("order_by", "score")
                        .queryParam("sort", "desc")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new MangaExternalApiException("Jikan API respondió con error: " + res.getStatusCode());
                })
                .body(new ParameterizedTypeReference<JikanPaginationResponse<JikanMangaData>>() {});

        return response != null ? response.data() : List.of();
    }

    // 2. NUEVO MÉTODO: OBTENER POR ID (Para el endpoint de detalle)
    public Optional<JikanMangaData> fetchMangaById(Long id) {
        try {
            var response = restClient.get()
                .uri("/manga/{id}/full", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    log.warn("Manga con ID {} no encontrado en Jikan", id);
                })
                .body(new ParameterizedTypeReference<JikanSingleResponse<JikanMangaData>>() {});

            return Optional.ofNullable(response).map(JikanSingleResponse::data);
        } catch (Exception e) {
            log.error("Error al obtener detalle del manga {} desde Jikan", id, e);
            return Optional.empty();
        }
    }

    public List<MangaRecommendationResponse> fetchRecommendations(Long malId) {
    // La URL de Jikan para recomendaciones
        String url = String.format("%s/manga/%d/recommendations", baseUrl, malId);

        try {
            log.debug("Llamando a Jikan Recommendations: {}", url);

            JikanRecommendationWrapper response = restClient.get()
                .uri(url)
                .retrieve()
                .body(JikanRecommendationWrapper.class);

            // Si la respuesta o la data son nulas, evitamos el NPE devolviendo lista vacía
            if (response == null || response.data() == null) {
                return Collections.emptyList();
            }

            return response.data().stream()
                .map(item -> new MangaRecommendationResponse(
                    item.entry().malId(),
                    item.entry().title(),
                    item.entry().images().jpg().imageUrl(),
                    item.entry().url(), // Aquí usamos la URL del manga específico
                    item.votes()
                ))
                .toList();

        } catch (Exception e) {
            log.error("Error al obtener recomendaciones de Jikan para el ID {}: {}", malId, e.getMessage());
            return Collections.emptyList();
        }
    }
}