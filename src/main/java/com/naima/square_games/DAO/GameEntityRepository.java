package com.naima.square_games.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameEntityRepository extends JpaRepository<GameEntity,String> {
    // Aucune méthode à écrire : Spring Data génère tout le CRUD à l'exécution !
}
