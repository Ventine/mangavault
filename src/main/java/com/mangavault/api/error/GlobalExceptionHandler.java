package com.mangavault.api.error;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MangaBaseException.class)
    public ProblemDetail handleMangaException(MangaBaseException ex) {
        // Aprovechamos los campos de nuestra jerarquía
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setTitle(ex.getTitle());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // Catch-all para errores inesperados (NullPointer, etc.)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUncaught(Exception ex) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "Ocurrió un error inesperado en el servidor."
        );
    }
}