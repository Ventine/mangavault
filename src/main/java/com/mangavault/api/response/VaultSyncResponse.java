package com.mangavault.api.response;

import java.util.List;

public record VaultSyncResponse(
    int totalProcessed,
    int updatedCount,
    int failedCount,
    List<String> details
) {}