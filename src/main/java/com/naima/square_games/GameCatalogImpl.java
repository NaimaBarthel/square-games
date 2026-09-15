package com.naima.square_games;

import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class GameCatalogImpl implements GameCatalog{

    //Instance du jeu TicTacToe du campus
    private final TicTacToeGameFactory ticTacToeGameFactory = new TicTacToeGameFactory();

    @Override
    public Collection<String> getGameIds(){
        return List.of(ticTacToeGameFactory.getGameFactoryId());
    }


}
