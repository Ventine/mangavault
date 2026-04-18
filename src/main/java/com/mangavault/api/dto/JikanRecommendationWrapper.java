package com.mangavault.api.dto;

import java.util.List;

public record JikanRecommendationWrapper(
    List<RecommendationData> data
) {}