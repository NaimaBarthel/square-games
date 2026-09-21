package com.naima.square_games.dao.repositories;

import com.naima.square_games.dao.entities.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameEntityRepository extends JpaRepository<GameEntity,String> {
    // Aucune méthode à écrire : Spring Data génère tout le CRUD à l'exécution !
}
