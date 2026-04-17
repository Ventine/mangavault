package com.mangavault.api.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.mangavault.api.dto.FavoriteManga;
import com.mangavault.api.dto.JikanGateway;
import com.mangavault.api.dto.JikanMangaData;
import com.mangavault.api.error.MangaNotFoundException;
import com.mangavault.api.repository.FavoriteMangaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MangaVaultService {

    private final FavoriteMangaRepository repository;
    private final JikanGateway jikanGateway;

    public FavoriteManga saveToVault(Long id) {
    try {
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
        // Log detallado para ver la causa real en la terminal de Docker
        throw new RuntimeException("Fallo en la persistencia o comunicación con el servidor");
    }
}
}