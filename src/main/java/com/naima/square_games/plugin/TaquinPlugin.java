package com.naima.square_games.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;
import fr.le_campus_numerique.square_games.engine.InconsistentGameDefinitionException;
import fr.le_campus_numerique.square_games.engine.taquin.TaquinGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TaquinPlugin implements GamePlugin {

    private final GameFactory factory = new TaquinGameFactory();
    private final MessageSource messageSource;

    @Value("${game.taquin.default-player-count}")
    private int defaultPlayerCount;

    @Value("${game.taquin.default-board-size}")
    private int defaultBoardSize;

    public TaquinPlugin(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getId() {
        return factory.getGameFactoryId();
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.taquin.name", null, locale);
    }

    @Override
    public Game createGame(Integer playerCount, Integer boardSize) {
        int actualPlayerCount = (playerCount != null) ? playerCount : defaultPlayerCount;
        int actualBoardSize = (boardSize != null) ? boardSize : defaultBoardSize;
        return factory.createGame(actualPlayerCount,actualBoardSize);
    }

    /**
     * Crée une partie de Morpion en associant explicitement les identifiants des joueurs aux jetons.
     *
     * @param playerIds liste des identifiants des joueurs (au moins 2 joueurs requis pour le Morpion).
     * @param boardSize dimension du plateau (ou taille par défaut si null).
     * @return l'instance de {@link Game} initialisée.
     */
    @Override
    public Game createGame(Collection<UUID> playerIds, Integer boardSize){
        int actualBoardSize = (boardSize != null) ? boardSize : defaultBoardSize;
        return factory.createGame(actualBoardSize, (Set<UUID>) playerIds);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reconstruit une partie de Taquin en préservant son identifiant unique persistant.
     * Pour ce jeu solitaire, la fabrique attend le joueur unique associé à la partie.
     * </p>
     *
     * @param gameId identifiant unique d'origine de la partie
     * @param players liste contenant l'identifiant du joueur
     * @param boardSize dimension du plateau (ou taille par défaut configurée si absente)
     * @return l'instance de {@link Game} réinitialisée avec l'identifiant préservé
     * @throws RuntimeException si les paramètres violent les règles de la fabrique du Taquin
     */
    @Override
    public Game reloadGame(UUID gameId, List<UUID> players, int boardSize) {
        int actualBoardSize = (boardSize > 0) ? boardSize : defaultBoardSize;
        try {
            return factory.createGameWithIds(gameId, actualBoardSize, players, List.of(), List.of());
        } catch (InconsistentGameDefinitionException e) {
            throw new RuntimeException("Erreur lors de la reconstruction de la partie de Taquin : " + gameId, e);
        }
    }
}