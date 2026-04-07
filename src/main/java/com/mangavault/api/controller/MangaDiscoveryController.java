package com.mangavault.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.mangavault.api.response.MangaDetailResponse;
import com.mangavault.api.response.MangaResponse;
import com.mangavault.api.service.MangaDiscoveryService;

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
}