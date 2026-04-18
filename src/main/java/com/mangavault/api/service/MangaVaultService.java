package com.mangavault.api.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mangavault.api.dto.FavoriteManga;
import com.mangavault.api.dto.JikanGateway;
import com.mangavault.api.dto.JikanMangaData;
import com.mangavault.api.error.MangaNotFoundException;
import com.mangavault.api.repository.FavoriteMangaRepository;
import com.mangavault.api.response.FavoriteMangaResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MangaVaultService {

    private final FavoriteMangaRepository repository;
    private final JikanGateway jikanGateway;

    public FavoriteManga saveToVault(Long id) {
        try {
            System.out.println("--- INICIO DE saveToVault ---");
            // 1. Verificación de existencia
            if (repository.existsById(id)) {
                return repository.findById(id).orElseThrow();
            }

            // 2. Obtención de datos externos
            JikanMangaData externalData = jikanGateway.fetchMangaById(id)
                    .orElseThrow(() -> new MangaNotFoundException("Manga no encontrado en Jikan"));

            // 3. Mapeo SEGURO (Null-Safe)
            // [SENIOR MOVE]: Usamos operadores ternarios para evitar el NullPointerException
            String safeImageUrl = (externalData.images() != null && 
                                externalData.images().webp() != null) 
                                ? externalData.images().webp().imageUrl() 
                                : "https://via.placeholder.com/225x350?text=No+Image";

            FavoriteManga favorite = new FavoriteManga(
                externalData.malId(),
                externalData.title(),
                safeImageUrl,
                externalData.score(),
                externalData.status(),
                LocalDateTime.now()
            );

            // 4. Persistencia
            return repository.save(favorite);

        } catch (MangaNotFoundException e) {
            throw e; // El GlobalExceptionHandler lo capturará como 404
        } catch (Exception e) {
            log.error("--- ERROR DETECTADO ---");
            log.error("Causa: {}", e.getMessage());
            e.printStackTrace(); // Esto llenará tu terminal de Docker con la solución
            throw e; 
        }
    }

    public Page<FavoriteMangaResponse> getFavoriteMangas(Pageable pageable) {
        log.info("--- INICIO DE CONSULTA DE FAVORITOS ---");
        log.debug("Consultando página {} con tamaño {}", pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<FavoriteManga> pagedResult = repository.findAll(pageable);
            
            log.info("Consulta exitosa. Se encontraron {} mangas en total.", pagedResult.getTotalElements());

            // Mapeamos la entidad al DTO
            return pagedResult.map(manga -> new FavoriteMangaResponse(
                manga.id(), 
                manga.title(),
                manga.imageUrl(),
                manga.score(),
                manga.status(),
                manga.addedAt()
            ));
        } catch (Exception e) {
            log.error("ERROR CRÍTICO al consultar favoritos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al recuperar los mangas de la base de datos");
        }
    }

    public void removeFromVault(Long id) {
        log.info("--- INICIO DE ELIMINACIÓN DE LA BÓVEDA ---");
        log.debug("Intentando eliminar manga con ID: {}", id);

        // 1. Verificamos si el recurso existe antes de actuar
        if (!repository.existsById(id)) {
            log.warn("Intento de eliminación fallido: El manga con ID {} no existe en la base de datos.", id);
            throw new RuntimeException("No se puede eliminar: el manga con ID " + id + " no está en tus favoritos.");
        }

        try {
            repository.deleteById(id);
            log.info("ÉXITO: Manga con ID {} eliminado correctamente.", id);
        } catch (Exception e) {
            log.error("ERROR CRÍTICO al eliminar de la base de datos: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno al intentar eliminar el registro.");
        }
    }
}