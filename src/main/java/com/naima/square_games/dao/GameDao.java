package com.naima.square_games.dao;

import fr.le_campus_numerique.square_games.engine.Game;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Contrat d'accès aux données pour la persistance des parties de jeu.
 */
public interface GameDao {

    /**
     * Récupère l'ensemble des parties stockées sous forme de flux.
     *
     * @return un flux contenant toutes les instances de {@link Game}.
     */
    Stream<Game> findAll();

    /**
     * Recherche une partie via son identifiant technique unique.
     *
     * @param gameId l'identifiant de la partie.
     * @return un {@link Optional} contenant la partie si trouvée, ou vide sinon.
     */
    Optional<Game> findById(String gameId);

    /**
     * Enregistre ou met à jour l'état d'une partie.
     *
     * @param game la partie à sauvegarder.
     * @return la partie enregistrée.
     */
    Game upsert(Game game);

    /**
     * Supprime une partie à partir de son identifiant.
     *
     * @param gameId l'identifiant de la partie à supprimer.
     */
    void delete(String gameId);
}