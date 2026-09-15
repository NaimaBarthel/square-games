package com.naima.square_games;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface GameService {
    Game createGame(GameCreationParams params);
    Optional<Game> getGame(UUID gameId);
    Collection<CellPosition> getAllowedMoves(UUID gameId, String tokenId);
    Game makeMove(UUID gameId, MoveParams moveParams) throws InvalidPositionException;
}
