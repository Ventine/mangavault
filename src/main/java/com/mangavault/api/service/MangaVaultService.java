package com.mangavault.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import com.mangavault.api.dto.FavoriteManga;
import com.mangavault.api.dto.JikanGateway;
import com.mangavault.api.dto.JikanMangaData;
import com.mangavault.api.error.MangaNotFoundException;
import com.mangavault.api.repository.FavoriteMangaRepository;
import com.mangavault.api.response.FavoriteMangaResponse;
import com.mangavault.api.response.MangaRecommendationResponse;
import com.mangavault.api.response.VaultStatsResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MangaVaultService {

    private final FavoriteMangaRepository repository;
    private final JikanGateway jikanGateway;
    private final String COLLECTION_NAME = "favorite_mangas";
    private final MongoTemplate mongoTemplate;

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

    public VaultStatsResponse getVaultStats() {
        log.info("--- GENERANDO ESTADÍSTICAS DE LA BÓVEDA ---");

        // Definimos los sub-pipelines
        AggregationOperation generalStats = Aggregation.group()
            .count().as("totalMangas")
            .avg("score").as("averageScore");

        AggregationOperation statusStats = Aggregation.group("status")
            .count().as("count");

        // REFACTOR: Todo en una sola etapa FacetOperation
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.facet(generalStats).as("general")
                    .and(statusStats).as("byStatus") // <--- ESTA ES LA CLAVE
        );

        try {
            AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
            Map<String, Object> rawResult = results.getUniqueMappedResult();

            // Verificación de nulidad antes de procesar
            if (rawResult == null) {
                return new VaultStatsResponse(0, 0.0, new HashMap<>());
            }

            return mapToResponse(rawResult);
        } catch (Exception e) {
            log.error("Error en agregación MongoDB: {}", e.getMessage());
            throw new RuntimeException("Error al calcular estadísticas");
        }
    }

    private VaultStatsResponse mapToResponse(Map<String, Object> rawResult) {
        // Extraemos con seguridad usando Optional o verificando nulos
        List<Map> general = (List<Map>) rawResult.getOrDefault("general", new ArrayList<>());
        List<Map> byStatus = (List<Map>) rawResult.getOrDefault("byStatus", new ArrayList<>());

        long total = 0;
        double avg = 0.0;
        Map<String, Long> statusMap = new HashMap<>();

        // Si hay datos generales
        if (general != null && !general.isEmpty()) {
            Map stats = general.get(0);
            total = stats.get("totalMangas") != null ? ((Number) stats.get("totalMangas")).longValue() : 0;
            avg = stats.get("averageScore") != null ? ((Number) stats.get("averageScore")).doubleValue() : 0.0;
        }

        // Procesamos el desglose por status
        if (byStatus != null) {
            for (Map entry : byStatus) {
                Object statusId = entry.get("_id");
                String status = (statusId != null) ? statusId.toString() : "Unknown";
                long count = entry.get("count") != null ? ((Number) entry.get("count")).longValue() : 0;
                statusMap.put(status, count);
            }
        }

        return new VaultStatsResponse(total, Math.round(avg * 100.0) / 100.0, statusMap);
    }

    public List<MangaRecommendationResponse> getSmartRecommendations(Long id) {
        log.info("--- GENERANDO RECOMENDACIONES INTELIGENTES PARA ID {} ---", id);

        // 1. Obtener recomendaciones de la API externa
        List<MangaRecommendationResponse> recommendations = jikanGateway.fetchRecommendations(id);

        if (recommendations.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. [SENIOR MOVE] Obtener todos los IDs de mis favoritos para filtrar
        // Así evitamos recomendar algo que el usuario ya guardó
        Set<Long> existingIds = repository.findAll().stream()
                .map(FavoriteManga::id)
                .collect(Collectors.toSet());

        // 3. Filtrar y limitar a las mejores 10 recomendaciones
        return recommendations.stream()
                .filter(rec -> !existingIds.contains(rec.id()))
                .sorted(Comparator.comparing(MangaRecommendationResponse::votes).reversed())
                .limit(10)
                .toList();
    }

}