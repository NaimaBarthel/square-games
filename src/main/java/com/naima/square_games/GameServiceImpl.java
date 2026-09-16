package com.naima.square_games;

import com.naima.square_games.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService{
    //Stockage en mémoire des parties en cours (clé : UUID de la partie, valeur : le Game )
    private final Map<UUID, Game> games = new ConcurrentHashMap<>();

    /**est remplacé par utilisation de GamePlugin
    //Factory pour fabriquer le jeu TicTacToe
    private final TicTacToeGameFactory ticTacToeGameFactory = new TicTacToeGameFactory();
    */
    // Table de correspondance : clé = "tictactoe", valeur = instance de TicTacToePlugin
    private final Map<String, GamePlugin> plugins;

    // Spring injecte automatiquement tous les beans qui implémentent GamePlugin
    public GameServiceImpl(List<GamePlugin> pluginList) {
        // 1. On initialise une Map vide
        this.plugins = new HashMap<>();

        // 2. On parcourt chaque plugin de la liste un par un
        for (GamePlugin plugin : pluginList) {
            // 3. On range le plugin dans la Map :
            // Clé = son identifiant (ex: "tictactoe")
            // Valeur = le plugin lui-même
            this.plugins.put(plugin.getId(), plugin);
        }
    }
    //En Java moderne
    /*public GameServiceImpl(List<GamePlugin> pluginList) {
        this.plugins = pluginList.stream()
                .collect(Collectors.toMap(GamePlugin::getId, plugin -> plugin));
    }*/


    /**
     * Crée et initialise une nouvelle partie de jeu avec les paramètres fournis,
     * puis l'enregistre en mémoire.
     * <p>
     * Applique des valeurs par défaut si les paramètres transmis sont invalides
     * ou non renseignés (2 joueurs et plateau 3x3 pour le Morpion).
     *
     * @param params paramètres de configuration de la partie à créer
     * @return l'instance du jeu {@link Game} nouvellement créée et stockée
     */
    @Override
    public Game createGame(GameCreationParams params) {
        /**est remplacé par utilisation de GamePlugin
        // Sécurité sur les paramètres par défaut si le client n'envoie rien ou des valeurs invalides
        int playerCount = (params.playerCount() > 0) ? params.playerCount() : 2;
        int boardSize = (params.boardSize() > 0) ? params.boardSize() : 3;

        // Appel de la factory du moteur de jeu
        Game game = ticTacToeGameFactory.createGame(playerCount,boardSize);
        */
        System.out.println(">>> Params reçus : " + params);
        System.out.println(">>> Plugins chargés dans la map : " + plugins.keySet());
        // 1. On cherche le plugin correspondant à l'identifiant demandé (ex: "tictactoe")
        GamePlugin plugin = plugins.get(params.gameType());

        if (plugin == null){
            throw new IllegalArgumentException("Type de jeu inconnu " + params.gameType());
        }
        // 2. On délègue la création au plugin (qui applique les valeurs par défaut si les paramètres sont null/vides)
        Game game = plugin.createGame(params.playerCount(), params.boardSize());

        // 3. On stocke la partie créée // Sauvegarde dans la Map en mémoire
        games.put(game.getId(),game);

        return game;
    }



    /**
     * Récupère une partie existante à partir de son identifiant unique
     *
     * @param gameId identifiant unique de la partie recherchée
     * @return un {@link Optional} contenant la partie correspondante si trouvée, ou un {@link Optional#empty()} sinon
     *
     */
    @Override
    public Optional<Game> getGame(UUID gameId) {
        Game game = games.get(gameId);
        return ((game != null) ? Optional.of(game) : Optional.empty());
    }


    /**
     * Récupère la liste des positions autorisées sur le plateau pour un jeton donné dans une partie.
     * <p>
     * La recherche s'effectue parmi les jetons en attente d'être joués (non encore placés sur le plateau).
     * Si la partie n'existe pas, ou si le jeton n'est pas trouvé parmi les jetons restants,
     * la méthode retourne une collection vide.
     * </p>
     *
     * @param gameId l'identifiant unique ({@link UUID}) de la partie concernée.
     * @param tokenId le nom ou l'identifiant du jeton dont on souhaite connaître les coups possibles (ex. "X" ou "0").
     * @return une {@link Collection} de {@link CellPosition} représentant les coordonnées accessibles pour ce jeton,
     *         ou un ensemble vide si la partie ou le jeton n'existe pas, ou si aucun coup n'est autorisé.
     */
    @Override
    public Collection<CellPosition> getAllowedMoves(UUID gameId, String tokenId) {
        Game game = games.get(gameId);
        if (game == null) {
            return Set.of(); // Si la partie n'existe pas, liste vide
        }

        // Recherche du jeton dans les jetons restants
        return game.getRemainingTokens().stream()
                .filter(token -> token.getName().equals(tokenId)) // ou token.getId() selon votre moteur
                .findFirst()
                .map(Token::getAllowedMoves)
                .orElse(Set.of());
    }

    /**
     * Exécute un coup sur le plateau de jeu en déplaçant le jeton du joueur actif.
     * <p>
     * La méthode sélectionne automatiquement le jeton qui a actuellement le droit
     * de jouer (en tête de file d'attente), puis tente de le placer à la position demandée.
     * </p>
     *
     * @param gameId l'identifiant unique ({@link UUID}) de la partie en cours.
     * @param moveParams les paramètres du coup contenant notamment la {@link fr.le_campus_numerique.square_games.engine.CellPosition} ciblée.
     * @return l'instance de {@link Game} mise à jour avec le jeton placé sur le plateau.
     * @throws java.util.NoSuchElementException si aucune partie ne correspond au {@code gameId} fourni.
     * @throws IllegalStateException si aucun jeton n'est en état de jouer (ex. partie déjà terminée ou aucun coup possible).
     * @throws InvalidPositionException si la case demandée est invalide (hors limites du plateau ou déjà occupée).
     */
    @Override
    public Game makeMove(UUID gameId, MoveParams moveParams) throws InvalidPositionException {
        Game game = games.get(gameId);
        // On vérifie si la partie demandée existe dans la Map en mémoire
        if(game == null){
            //Si la partie n'existe pas, on lève une exception
            throw new NoSuchElementException("Partie introuvable : "+ gameId);
        }

        //Récupérer le jeton actif (celui en tête qui a le droit de jouer)
        Token currentToken = game.getRemainingTokens().stream()
                .filter(Token::canMove)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucun coup possible actuellement"));

        //Exécuter le coup sur le plateau
        currentToken.moveTo(moveParams.position());

        return game;

    }
}
