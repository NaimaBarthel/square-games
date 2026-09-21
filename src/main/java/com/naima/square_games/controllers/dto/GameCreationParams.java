package com.naima.square_games.controllers.dto;

public record GameCreationParams(String gameType,
                                 Integer playerCount,
                                 Integer boardSize) {
}

