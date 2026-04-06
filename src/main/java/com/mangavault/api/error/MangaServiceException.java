package com.mangavault.api.error;

import org.springframework.http.HttpStatus;

public class MangaServiceException extends MangaBaseException {
    public MangaServiceException(String message) {
        super("Error Interno del Servicio", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}