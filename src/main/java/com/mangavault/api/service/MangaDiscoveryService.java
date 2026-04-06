package com.mangavault.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mangavault.api.dto.JikanGateway;
import com.mangavault.api.dto.JikanMangaData;
import com.mangavault.api.error.MangaExternalApiException;
import com.mangavault.api.error.MangaServiceException;
import com.mangavault.api.response.MangaResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor // Inyección por constructor automática
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
            // Re-lanzamos o manejamos según la política de negocio
            throw e; 
        } catch (Exception e) {
            log.error("Error inesperado procesando búsqueda", e);
            throw new MangaServiceException("Error interno al procesar los datos del manga");
        }
    }

    private MangaResponse mapToResponse(JikanMangaData data) {
        return new MangaResponse(
            data.malId(),
            data.title(),
            data.synopsis(),
            data.images().webp().imageUrl(),
            data.score(),
            data.chapters()
        );
    }
}