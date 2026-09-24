package com.naima.square_games.services;

import com.naima.square_games.controllers.dto.GameCreationParams;
import com.naima.square_games.controllers.dto.MoveParams;
import com.naima.square_games.plugin.GamePlugin;
import com.naima.square_games.dao.GameDao;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameServiceImpl implements GameService{
    /* A supprimer pour injecter DAO
    //Stockage en mémoire des parties en cours (clé : UUID de la partie, valeur : le Game )
    //private final Map<UUID, Game> games = new ConcurrentHashMap<>();
    */

    // Référence vers le DAO pour déléguer les opérations de persistance (stockage/lecture)
    private final GameDao gameDao;

    // Table de correspondance associant l'identifiant technique d'un jeu (clé) à son plugin (valeur)
    // Table de correspondance : clé = "tictactoe", valeur = instance de TicTacToePlugin
    private final Map<String, GamePlugin> plugins;

    //Déclaration du client
    private final UserRestClient userRestClient;

    // Spring injecte automatiquement tous les beans qui implémentent GamePlugin
    public GameServiceImpl(GameDao gameDao, List<GamePlugin> pluginList, UserRestClient userRestClient, UserRestClient userRestClient1) {
        //Initialisation du DAO injecté par Spring
        this.gameDao = gameDao;

        //On initialise une Map vide
        this.plugins = new HashMap<>();

        this.userRestClient = userRestClient1;

        //On parcourt chaque plugin de la liste un par un
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
     * Crée et initialise une nouvelle partie après validation de l'utilisateur demandeur
     * auprès du microservice distant square-users, puis l'enregistre en base de données.
     * L'identifiant de l'utilisateur demandeur est placé en tête de la liste des joueurs
     * pour lui attribuer la main et le premier jeton.
     *
     * @param userId identifiant du joueur créateur extrait de l'en-tête HTTP X-UserId.
     * @param params paramètres de configuration de la partie demandée (type de jeu, taille du plateau, etc.).
     * @return l'instance de {@link Game} nouvellement créée et persistée.
     * @throws SecurityException si l'identifiant {@code userId} n'existe pas dans le service des utilisateurs.
     * @throws IllegalArgumentException si le type de jeu demandé ne correspond à aucun plugin enregistré.
     */
    @Override
    public Game createGame(String userId, GameCreationParams params) {
        /**est remplacé par utilisation de GamePlugin
        // Sécurité sur les paramètres par défaut si le client n'envoie rien ou des valeurs invalides
        int playerCount = (params.playerCount() > 0) ? params.playerCount() : 2;
        int boardSize = (params.boardSize() > 0) ? params.boardSize() : 3;

        // Appel de la factory du moteur de jeu
        Game game = ticTacToeGameFactory.createGame(playerCount,boardSize);
        */
        //0. Vérification de l'existence de l'utilisateur via l'API distante square-users
        if(!userRestClient.checkUserExists(userId)){
            throw new SecurityException("Utilisateur non reconnu ou inexistant : " + userId);
        }

        System.out.println(">>> X-UserId validé : " + userId);
        System.out.println(">>> Params reçus : " + params);
        System.out.println(">>> Plugins chargés dans la map : " + plugins.keySet());

        // 1. On cherche le plugin correspondant à l'identifiant demandé (ex: "tictactoe")
        GamePlugin plugin = plugins.get(params.gameType());

        if (plugin == null){
            throw new IllegalArgumentException("Type de jeu inconnu " + params.gameType());
        }

        // 2. Préparation de la liste ordonnée des joueurs
        List<UUID> players = new ArrayList<>();
        players.add(UUID.fromString(userId));    // Le créateur est le premier joueur (joueur actif)

        //Ajout d'un adversaire par défaut (le 2e joueur) avec UUID créé aléatoirement
        players.add(UUID.randomUUID());

        System.out.println("GameServiceImpl -- createGame players : >>>> " + players.toString());
        // 3. Initialisation du plateau avec les joueurs définis
        Game game = plugin.createGame(players, params.boardSize());

        // 4. Persistance de la partie via le DAO (remplace l'ancien games.put())
        return gameDao.upsert(game);
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
        /** A supprimer pour injecter DAO
        Game game = games.get(gameId);
        return ((game != null) ? Optional.of(game) : Optional.empty());
        */
        // On délègue la recherche au DAO en convertissant l'UUID en String.
        // findById renvoie déjà un Optional<Game>, le code est direct et concis.
        return gameDao.findById(gameId.toString());
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
       /* A supprimer pour injecter DAO
        Game game = games.get(gameId);

        if (game == null) {
            return Set.of(); // Si la partie n'existe pas, liste vide
        }

        */
        // 1. Recherche de la partie via le DAO
        Optional<Game> gameOptional = gameDao.findById(gameId.toString());

        // 2. Si la partie n'existe pas, on renvoie une liste vide
        if (gameOptional.isEmpty()) {
            return Set.of();
        }

        //  3. On extrait l'instance réelle de Game contenue dans l'Optional (garantie présente après la vérification isEmpty)
        Game game = gameOptional.get();

        // Recherche du jeton dans les jetons restants
        return game.getRemainingTokens().stream()
                .filter(token -> token.getName().equals(tokenId)) // ou token.getId() selon votre moteur
                .findFirst()
                .map(Token::getAllowedMoves)
                .orElse(Set.of());
    }

    /**
     * Exécute un coup sur le plateau en déplaçant le jeton actif,
     * puis sauvegarde le nouvel état de la partie via le DAO.
     *
     * @param gameId identifiant unique de la partie
     * @param moveParams coordonnées de destination du coup
     * @return l'instance de {@link Game} mise à jour et persistée
     * @throws NoSuchElementException si la partie n'existe pas dans le DAO
     * @throws IllegalStateException si aucun jeton ne peut jouer
     * @throws InvalidPositionException si le coup est invalide selon les règles du jeu
     */
    @Override
    public Game makeMove(String userId,UUID gameId, MoveParams moveParams) throws InvalidPositionException {
         /* A supprimer pour injecter DAO
        Game game = games.get(gameId);
         // On vérifie si la partie demandée existe dans la Map en mémoire
        if(game == null){
            //Si la partie n'existe pas, on lève une exception
            throw new NoSuchElementException("Partie introuvable : "+ gameId);
        }
        */
        // 1. Récupération de la partie depuis le DAO ou levée d'une exception si absente
        Game game = gameDao.findById(gameId.toString())
                .orElseThrow(() -> new NoSuchElementException("Partie introuvable : " + gameId));

       // 2. Récupérer le jeton actif (celui en tête qui a le droit de jouer)
        Token currentToken = game.getRemainingTokens().stream()
                .filter(Token::canMove)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucun coup possible actuellement"));

        //3. VÉRIFICATION DU JOUEUR ACTIF (Contrainte HTTP 403)
        // Vérifie si le propriétaire du jeton actif correspond à l'en-tête X-UserId
        if(currentToken.getOwnerId().isEmpty() || !currentToken.getOwnerId().get().toString().equals(userId)){
            throw new SecurityException("Ce n'est pas votre tour de jouer !");
        }
        // 4. Exécuter le coup sur le plateau
        currentToken.moveTo(moveParams.position());
        /* A supprimer pour injecter DAO
        return game;
        */

        // 5. Enregistrement de l'état modifié dans le DAO et retour du jeu
        return gameDao.upsert(game);
    }

    /**
     * Récupère toutes les parties enregistrées dans le DAO, puis ne conserve
     * que celles dont la liste des joueurs contient l'identifiant fourni.
     *
     * @param userId l'identifiant du joueur (issu de X-UserId).
     * @return la collection filtrée des parties du joueur.
     */
    @Override
    public Collection<Game> getGamesForUser(String userId){
        UUID playerUuid = UUID.fromString(userId);

        List<Game> allGames = gameDao.findAll().toList();
        System.out.println(">>> TOTAL PARTIES DANS DAO : " + allGames.size());
        for (Game g : allGames) {
            System.out.println(">>> PARTIE : " + g.getId() + " - JOUEURS : " + g.getPlayerIds());
        }

        //On récupère toutes les parties du joueur userId depuis le DAO, et on filtre sur les joueurs
        return allGames.stream()
                .filter(game -> game.getPlayerIds().contains(playerUuid))
                .toList();

    }
}
