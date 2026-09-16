package com.naima.square_games;

import java.util.Collection;
import java.util.Locale;

/**
 * Contrat définissant le catalogue des types de jeux disponibles dans l'application.
 */
public interface GameCatalog {
    /**
     * Récupère la liste des identifiants techniques uniques de tous les jeux enregistrés.
     *
     * @return une collection contenant les identifiants techniques (ex: "tictactoe", "connect4").
     */
    Collection<String> getGameIds();


    /**
     * Récupère les libellés traduits des jeux enregistrés en fonction de la région linguistique demandée.
     *
     * @param locale la langue ou région ciblée (issue de l'en-tête Accept-Language).
     * @return une collection contenant les noms des jeux traduits (ex: "Morpion", "Puissance 4").
     */
    Collection<String> getGameNames(Locale locale);
}
