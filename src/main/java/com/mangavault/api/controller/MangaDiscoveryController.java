package com.mangavault.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mangavault.api.response.MangaResponse;
import com.mangavault.api.service.MangaDiscoveryService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/mangas")
@RequiredArgsConstructor
@Validated
public class MangaDiscoveryController {

    private final MangaDiscoveryService mangaService;

    @GetMapping("/search")
    public ResponseEntity<List<MangaResponse>> search(
            @RequestParam @NotBlank @Size(min = 3, message = "La búsqueda debe tener al menos 3 caracteres") String q) {
        
        List<MangaResponse> results = mangaService.searchMangas(q);
        
        return results.isEmpty() 
                ? ResponseEntity.noContent().build() 
                : ResponseEntity.ok(results);
    }
}