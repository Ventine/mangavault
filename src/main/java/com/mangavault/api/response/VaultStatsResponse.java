package com.mangavault.api.response;

import java.util.Map;

public record VaultStatsResponse(
    long totalMangas,
    double averageScore,
    Map<String, Long> countByStatus
) {}