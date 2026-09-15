package com.naima.square_games;

import fr.le_campus_numerique.square_games.engine.Game;
import java.util.Collection;
import java.util.UUID;

public record GameDto(
        UUID id,
        String factoryId,
        Collection<UUID> playerIds,
        int boardSize
) {
    public static GameDto fromGame(Game game) {
        return new GameDto(
                game.getId(),
                game.getFactoryId(),
                game.getPlayerIds(),
                game.getBoardSize()
        );
    }
}
