package com.mangavault.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

record JpgWrapper(
    @JsonProperty("image_url") String imageUrl
) {}