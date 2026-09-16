package com.naima.square_games.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import java.util.Locale;

/**
 * Contrat définissant le comportement d'un plugin de jeu intégrable au catalogue.
 */
public interface GamePlugin
{
    /**
     * Identifiant unique du type de jeu (ex: "tictactoe", "taquin", "connectfour").
     */
    String getId();

    /**
     * Retourne le nom d'affichage du jeu traduit selon la langue passée en paramètre.
     *
     * @param locale la langue cible (français, anglais, etc.)
     * @return le libellé traduit du jeu
     */
    String getName(Locale locale);

    /**
     * Crée une nouvelle partie.
     * Si un paramètre vaut null, l'implémentation utilisera sa valeur par défaut.
     *
     * @param playerCount nombre de joueurs souhaité (peut être null)
     * @param boardSize dimension du plateau souhaitée (peut être null)
     * @return la partie initialisée
     */
    Game createGame(Integer playerCount, Integer boardSize);

}
