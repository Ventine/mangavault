package com.mangavault.api.response;

public record JikanSingleResponse<T>(
    T data
) {}