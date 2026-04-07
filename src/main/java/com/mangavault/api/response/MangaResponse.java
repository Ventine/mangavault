package com.mangavault.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta resumida de un Manga (ideal para listados y búsquedas)")
public record MangaResponse(
    @Schema(description = "ID único del manga en la base de datos externa", example = "1") 
    Long id,
    
    @Schema(description = "Título principal del manga", example = "Monster") 
    String title,
    
    @Schema(description = "Sinopsis de la trama", example = "Kenzou Tenma es un cirujano japonés...") 
    String synopsis,
    
    @Schema(description = "URL de la portada en formato WebP", example = "https://cdn.myanimelist.net/images/manga/3/258224.webp") 
    String imageUrl,
    
    @Schema(description = "Puntuación global dada por los usuarios", example = "8.9") 
    Double score,
    
    @Schema(description = "Número total de capítulos", example = "162") 
    Integer chapters
) {}