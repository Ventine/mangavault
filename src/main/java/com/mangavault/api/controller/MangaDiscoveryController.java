package com.mangavault.api.controller;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mangavault.api.dto.FavoriteManga;
import com.mangavault.api.dto.JikanGateway;
import com.mangavault.api.response.ApiStatusResponse;
import com.mangavault.api.response.FavoriteMangaResponse;
import com.mangavault.api.response.MangaDetailResponse;
import com.mangavault.api.response.MangaRecommendationResponse;
import com.mangavault.api.response.MangaResponse;
import com.mangavault.api.response.VaultStatsResponse;
import com.mangavault.api.service.MangaDiscoveryService;
import com.mangavault.api.service.MangaVaultService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/mangas")
@RequiredArgsConstructor
@Validated
@Tag(name = "Manga Discovery", description = "Endpoints para buscar y explorar nuevos mangas desde fuentes externas")
public class MangaDiscoveryController {

    private final MangaDiscoveryService mangaService;
    private final MongoTemplate mongoTemplate;
    private final JikanGateway jikanGateway;
    private final MangaVaultService vaultService;
    
    @Operation(summary = "Buscar mangas por nombre", description = "Realiza una búsqueda en la API externa de Jikan filtrando por relevancia.")
    @ApiResponse(responseCode = "200", description = "Búsqueda exitosa")
    @ApiResponse(responseCode = "204", description = "No se encontraron resultados")
    @GetMapping("/search")
    public ResponseEntity<List<MangaResponse>> search(
            @Parameter(description = "Nombre o palabra clave del manga", example = "Naruto")
            @RequestParam @NotBlank @Size(min = 3, message = "La búsqueda debe tener al menos 3 caracteres") String q) {
        
        List<MangaResponse> results = mangaService.searchMangas(q);
        return results.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(results);
    }

    @Operation(summary = "Obtener detalle completo", description = "Recupera toda la información técnica, géneros y estadísticas de un manga por su ID.")
    @ApiResponse(responseCode = "200", description = "Detalle encontrado")
    @ApiResponse(responseCode = "404", description = "El ID proporcionado no existe")
    @GetMapping("/{id}")
    public ResponseEntity<MangaDetailResponse> getById(
            @Parameter(description = "ID único del manga (MAL ID)", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(mangaService.getMangaById(id));
    }
 
    @Operation(summary = "Guardar en la bóveda", description = "Guarda un manga de la API externa en tu base de datos local de MongoDB.")
    @ApiResponse(responseCode = "201", description = "Manga guardado exitosamente")
    @PostMapping("/vault/{id}")
    public ResponseEntity<FavoriteManga> addToVault(@PathVariable Long id) {
        FavoriteManga savedManga = vaultService.saveToVault(id);
        return new ResponseEntity<>(savedManga, HttpStatus.CREATED);
    }

    @Operation(summary = "Check de salud profundo", description = "Verifica la conectividad con MongoDB y la API de Jikan.")
    @GetMapping("/status")
    public ResponseEntity<ApiStatusResponse> getStatus() {
        Map<String, String> deps = new HashMap<>();

        // 1. Verificar MongoDB
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            deps.put("database", "UP (MongoDB Connected)");
        } catch (Exception e) {
            deps.put("database", "DOWN (Error connecting)");
        }

        // 2. Verificar Jikan (haciendo una llamada ligera al ID 1)
        boolean jikanUp = jikanGateway.fetchMangaById(1L).isPresent();
        deps.put("external_api_jikan", jikanUp ? "UP" : "DOWN/THROTTLED");

        ApiStatusResponse response = new ApiStatusResponse(
            "OPERATIONAL",
            "0.0.1-SNAPSHOT",
            LocalDateTime.now(),
            calculateUptime(),
            deps
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/vault") 
    public ResponseEntity<Page<FavoriteMangaResponse>> getAllFavorites(
        @PageableDefault(
            size = 10, 
            sort = "addedAt", 
            direction = Direction.DESC 
        ) Pageable pageable) {
            
        Page<FavoriteMangaResponse> favorites = vaultService.getFavoriteMangas(pageable);

        if (favorites.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(favorites);
    }

    @Operation(
        summary = "Eliminar de la bóveda", 
        description = "Borra un manga de tus favoritos en MongoDB usando su ID único."
    )
    @ApiResponse(responseCode = "204", description = "Manga eliminado exitosamente (sin contenido de vuelta)")
    @ApiResponse(responseCode = "404", description = "El ID proporcionado no se encontró en la base de datos")
    @DeleteMapping("/vault/{id}") // <--- Verbo correcto para eliminar
    public ResponseEntity<Void> removeFromVault(
            @Parameter(description = "ID del manga a eliminar", example = "28")
            @PathVariable Long id) {
                
        vaultService.removeFromVault(id);
        
        // Devolvemos 204 No Content: La forma más limpia de decir "Hecho"
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Obtener estadísticas de la bóveda", 
        description = "Calcula métricas agregadas como el total de mangas, promedio de score y distribución por estados."
    )
    @ApiResponse(responseCode = "200", description = "Estadísticas generadas correctamente")
    @GetMapping("/vault/stats")
    public ResponseEntity<VaultStatsResponse> getStats() {
        return ResponseEntity.ok(vaultService.getVaultStats());
    }

    @Operation(
        summary = "Obtener recomendaciones basadas en un favorito", 
        description = "Consulta la API de Jikan para obtener recomendaciones similares a un manga de tu bóveda, filtrando los que ya posees."
    )
        @ApiResponse(responseCode = "200", description = "Lista de recomendaciones generada")
        @ApiResponse(responseCode = "404", description = "El ID base no fue encontrado")
        @GetMapping("/vault/{id}/recommendations")
        public ResponseEntity<List<MangaRecommendationResponse>> getRecommendations(@PathVariable Long id) {
           
            List<MangaRecommendationResponse> recommendations = vaultService.getSmartRecommendations(id);
            
            return recommendations.isEmpty() ? 
                ResponseEntity.noContent().build() : 
                ResponseEntity.ok(recommendations);
    }

    private String calculateUptime() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%d hours, %d minutes, %d seconds", hours % 24, minutes % 60, seconds % 60);
    }
}