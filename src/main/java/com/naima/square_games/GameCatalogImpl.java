package com.naima.square_games;

import com.naima.square_games.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Service
public class GameCatalogImpl implements GameCatalog{

    private final List<GamePlugin> plugins;

    public GameCatalogImpl(List<GamePlugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public Collection<String> getGameIds(){
        return plugins.stream()
                .map(GamePlugin::getId)
                .toList();
    }

    @Override
    public Collection<String> getGameNames(Locale locale) {
        return plugins.stream()
                .map(plugin -> plugin.getName(locale))
                .toList();
    }
}
