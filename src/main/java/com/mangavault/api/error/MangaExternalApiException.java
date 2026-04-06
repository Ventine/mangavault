package com.mangavault.api.error;

import org.springframework.http.HttpStatus;

public class MangaExternalApiException extends MangaBaseException {
    public MangaExternalApiException(String message) {
        super("Error de Proveedor Externo", message, HttpStatus.BAD_GATEWAY);
    }
}
