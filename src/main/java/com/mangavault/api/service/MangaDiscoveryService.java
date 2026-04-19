package com.mangavault.api.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mangavault.api.dto.JikanGateway;
import com.mangavault.api.dto.JikanMangaData;
import com.mangavault.api.dto.JikanTopWrapper;
import com.mangavault.api.error.MangaExternalApiException; // Import corregido
import com.mangavault.api.error.MangaNotFoundException;
import com.mangavault.api.error.MangaServiceException;
import com.mangavault.api.response.MangaDetailResponse;
import com.mangavault.api.response.MangaResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MangaDiscoveryService {

    private final JikanGateway jikanGateway;

    public List<MangaResponse> searchMangas(String query) {
        log.info("Iniciando búsqueda de manga: {}", query);
        try {
            return jikanGateway.fetchMangasFromExternalApi(query)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        } catch (MangaExternalApiException e) {
            throw e; 
        } catch (Exception e) {
            log.error("Error inesperado procesando búsqueda para: {}", query, e);
            throw new MangaServiceException("Error interno al procesar los datos del manga");
        }
    }

    public MangaDetailResponse getMangaById(Long id) {
        log.info("Consultando detalles del manga ID: {}", id);
        return jikanGateway.fetchMangaById(id)
                .map(this::mapToDetailResponse)
                .orElseThrow(() -> new MangaNotFoundException("El manga con ID " + id + " no existe."));
    }

    private MangaResponse mapToResponse(JikanMangaData data) {
        return new MangaResponse(
            data.malId(),
            data.title(),
            data.synopsis(),
            data.images() != null && data.images().webp() != null ? data.images().webp().imageUrl() : null,
            data.score(),
            data.chapters()
        );
    }

    private MangaDetailResponse mapToDetailResponse(JikanMangaData data) {
        return new MangaDetailResponse(
            data.malId(),
            data.title(),
            data.synopsis(),
            data.type(),
            data.status(),
            data.score(),
            data.rank(),
            // [SENIOR MOVE] Manejo seguro de listas nulas
            data.genres() != null 
                ? data.genres().stream().map(JikanMangaData.Genre::name).toList() 
                : Collections.emptyList(),
            data.images() != null && data.images().webp() != null ? data.images().webp().imageUrl() : null
        );
    }

    public JikanTopWrapper getTopMangas(String type, String filter, Integer page) {
        return jikanGateway.fetchTopMangas(type, filter, page);
    }
}