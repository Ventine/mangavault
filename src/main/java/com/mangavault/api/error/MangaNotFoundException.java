package com.mangavault.api.error;

import org.springframework.http.HttpStatus;

public class MangaNotFoundException extends MangaBaseException {
    public MangaNotFoundException(String message) {
        // Inyectamos el título y el status 404 a la clase padre
        super("Manga No Encontrado", message, HttpStatus.NOT_FOUND);
    }
}