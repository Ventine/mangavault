package com.mangavault.api.dto;

import com.mangavault.api.response.JikanPaginationResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

import com.mangavault.api.error.MangaExternalApiException;

@Component
public class JikanGateway {

    private final RestClient restClient;

    public JikanGateway(
    RestClient.Builder builder, 
    @Value("${app.external-api.jikan.base-url:https://api.jikan.moe/v4}") String baseUrl) {
    
    this.restClient = builder.baseUrl(baseUrl).build();
}

    public List<JikanMangaData> fetchMangasFromExternalApi(String query) {
        var response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/manga")
                        .queryParam("q", query)
                        .queryParam("order_by", "score")
                        .queryParam("sort", "desc")
                        .build())
                .retrieve()
                // Aquí capturamos errores de red o del proveedor
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new MangaExternalApiException("Jikan API respondió con error: " + res.getStatusCode());
                })
                .body(new ParameterizedTypeReference<JikanPaginationResponse<JikanMangaData>>() {});

        return response != null ? response.data() : List.of();
    }
}