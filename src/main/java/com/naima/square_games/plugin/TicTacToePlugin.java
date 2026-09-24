package com.naima.square_games.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TicTacToePlugin implements GamePlugin {

    private final GameFactory factory = new TicTacToeGameFactory();
    private final MessageSource messageSource;

    @Value("${game.tictactoe.default-player-count}")
    private int defaultPlayerCount;

    @Value("${game.tictactoe.default-board-size}")
    private int defaultBoardSize;

    /**
     * Construit le plugin en injectant la source de messages pour l'internationalisation.
     *
     * @param messageSource composant de gestion des messages i18n.
     */
    public TicTacToePlugin(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getId() {
        return factory.getGameFactoryId();
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.tictactoe.name", null, locale);
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
         // 1. Conversion sécurisée de Collection en Set
         Set<UUID> playersSet = (playerIds instanceof Set<UUID> s) ? s : new LinkedHashSet<>(playerIds);
         return factory.createGame(actualBoardSize, playersSet);
     }


}