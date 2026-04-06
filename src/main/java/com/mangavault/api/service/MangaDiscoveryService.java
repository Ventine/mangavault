package com.mangavault.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mangavault.api.dto.JikanGateway;
import com.mangavault.api.dto.JikanMangaData;
import com.mangavault.api.error.MangaBaseException.MangaNotFoundException;
import com.mangavault.api.error.MangaExternalApiException;
import com.mangavault.api.error.MangaServiceException;
import com.mangavault.api.response.MangaDetailResponse;
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

    public MangaDetailResponse getMangaById(Long id) {
    log.info("Obteniendo detalles para el manga ID: {}", id);

    return jikanGateway.fetchMangaById(id)
            .map(this::mapToDetailResponse)
            .orElseThrow(() -> new MangaNotFoundException("El manga con ID " + id + " no existe."));
    }

    private MangaDetailResponse mapToDetailResponse(JikanMangaData data) {
        return new MangaDetailResponse(
            data.malId(),
            data.title(),
            data.synopsis(),
            data.type(), // Asegúrate de añadir estos campos a tu JikanMangaData DTO
            data.status(),
            data.score(),
            data.rank(),
            data.genres().stream().map(g -> g.name()).toList(),
            data.images().webp().imageUrl()
        );
    }
}