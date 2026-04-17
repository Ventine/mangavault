package com.mangavault.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Información detallada sobre el estado operativo de la API")
public record ApiStatusResponse(
    String status,
    String version,
    LocalDateTime serverTime,
    String uptime,
    Map<String, String> dependencies // Para mostrar si Mongo y Jikan están OK
) {}