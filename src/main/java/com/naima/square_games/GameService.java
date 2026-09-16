package com.naima.square_games;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat de service définissant la logique métier et les opérations
 * applicables aux parties de jeu.
 */
public interface GameService {
    /**
     * Crée et initialise une nouvelle partie selon les paramètres spécifiés.
     *
     * @param params paramètres de configuration (type de jeu, taille du plateau, joueurs).
     * @return l'instance du jeu {@link Game} créée et enregistrée.
     * @throws IllegalArgumentException si le type de jeu demandé n'est pas pris en charge.
     */
    Game createGame(GameCreationParams params);

    /**
     * Recherche une partie en cours via son identifiant unique.
     *
     * @param gameId l'identifiant universel unique ({@link UUID}) de la partie.
     * @return un {@link Optional} contenant la partie si trouvée, ou un {@link Optional#empty()} sinon.
     */
    Optional<Game> getGame(UUID gameId);

    /**
     * Calcule la liste des positions accessibles pour un jeton donné sur le plateau.
     *
     * @param gameId identifiant unique de la partie concernée.
     * @param tokenId identifiant ou symbole du jeton ciblé.
     * @return une collection de coordonnées {@link CellPosition} autorisées,
     *         ou une collection vide si la partie n'existe pas ou si aucun coup n'est possible.
     */
    Collection<CellPosition> getAllowedMoves(UUID gameId, String tokenId);

    /**
     * Exécute le déplacement ou le placement d'un jeton pour le joueur actif.
     *
     * @param gameId identifiant unique de la partie en cours.
     * @param moveParams coordonnées cibles du coup à jouer.
     * @return l'instance du jeu {@link Game} mise à jour après l'action.
     * @throws NoSuchElementException si aucune partie ne correspond à l'identifiant fourni.
     * @throws IllegalStateException si aucun jeton n'est en mesure de jouer ou si la partie est terminée.
     * @throws InvalidPositionException si la coordonnée ciblée est illégale ou occupée.
     */
    Game makeMove(UUID gameId, MoveParams moveParams) throws InvalidPositionException;
}
