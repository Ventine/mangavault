package com.mangavault.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository; 
import org.springframework.stereotype.Repository;

import com.mangavault.api.dto.FavoriteManga;

@Repository
public interface FavoriteMangaRepository extends MongoRepository<FavoriteManga, Long> {
    // Aquí ya tenemos save(), findById(), delete(), etc., gratis.
}