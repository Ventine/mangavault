package com.mangavault.api.error;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 1. Manejador Inteligente para TODAS nuestras excepciones de negocio
    @ExceptionHandler(MangaBaseException.class)
    public ProblemDetail handleMangaException(MangaBaseException ex) {
        // Extrae dinámicamente el 404, 502 o 500 según la excepción que llegue
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setTitle(ex.getTitle());
        problem.setProperty("timestamp", Instant.now());
        // Puedes añadir más propiedades customizadas aquí si lo deseas
        return problem;
    }

    // 2. Catch-all: El escudo final para errores que no previmos (ej. NullPointerException)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUncaught(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "Ocurrió un error inesperado en el servidor. Por favor, contacte a soporte."
        );
        problem.setTitle("Error Interno Crítico");
        problem.setProperty("timestamp", Instant.now());
        // En un entorno real, aquí deberíamos loguear el stacktrace real (ex.getMessage()) 
        // pero NO enviarlo al cliente por seguridad.
        return problem;
    }
}