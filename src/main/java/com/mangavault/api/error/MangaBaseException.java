package com.mangavault.api.error;

import org.springframework.http.HttpStatus;
import lombok.Getter;

/**
 * Clase base para todas las excepciones de negocio de MangaVault.
 */
@Getter
public abstract class MangaBaseException extends RuntimeException {
    private final String title;
    private final HttpStatus status;

    protected MangaBaseException(String title, String message, HttpStatus status) {
        super(message);
        this.title = title;
        this.status = status;
    }
}