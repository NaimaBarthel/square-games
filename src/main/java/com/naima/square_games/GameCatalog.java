package com.naima.square_games;

import java.util.Collection;
import java.util.Locale;

public interface GameCatalog {
    Collection<String> getGameIds();
    Collection<String> getGameNames(Locale locale);
}
