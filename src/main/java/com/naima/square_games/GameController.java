package com.naima.square_games;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    /**
     * Constructeur permettant l'injection de dépendances pour le contrôleur.
     * <p>
     * Spring Boot utilise ce constructeur pour injecter automatiquement
     * l'implémentation active de {@link GameService}.
     * </p>
     *
     * @param gameService le service métier gérant le cycle de vie et les actions des jeux.
     */
    public GameController(GameService gameService){
        this.gameService = gameService;
    }

    //EndPoint 1 : Créer une partie
    //@RequestBody indique à Spring de lire le corps JSON et de le convertir en GameCreationParams
    /**
     * Endpoint REST permettant d'initialiser et de créer une nouvelle partie.
     * <p>
     * Réceptionne les paramètres de configuration dans le corps de la requête JSON,
     * sollicite le moteur de jeu pour instancier la partie correspondante, puis
     * renvoie une vue simplifiée sous forme de {@link GameDto}.
     * </p>
     *
     * @param params les paramètres de création ({@link GameCreationParams}) transmis dans le corps HTTP,
     *               définissant notamment le type de jeu, les dimensions ou les joueurs.
     * @return un {@link GameDto} contenant les informations clés de la partie nouvellement créée.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameDto createGame(@RequestBody GameCreationParams params) {
        Game game = gameService.createGame(params);
        return GameDto.fromGame(game);
    }

    //EndPoint 2 : Récupérer l'état d'une partie
    //@PathVariable extrait l'id directement depuis l'URL /games/{gameId}
    /**
     * Endpoint REST permettant de récupérer l'état actuel d'une partie.
     * <p>
     * Recherche la partie correspondant à l'identifiant fourni dans l'URL.
     * Si elle existe, l'instance du jeu est sérialisée en JSON et renvoyée avec un code 200.
     * Dans le cas contraire, une réponse 404 est retournée.
     * </p>
     *
     * @param gameId l'identifiant unique ({@link UUID}) de la partie extrait du chemin d'URL.
     * @return une {@link ResponseEntity} contenant :
     *         <ul>
     *           <li>{@code 200 OK} avec l'objet {@link Game} si la partie est trouvée.</li>
     *           <li>{@code 404 Not Found} si aucune partie ne correspond à cet identifiant.</li>
     *         </ul>
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGame(@PathVariable UUID gameId){
      //  return gameService.getGame(gameId).map(ResponseEntity::ok).orElseGet(()->ResponseEntity.notFound().build());
        Optional<Game> gameOptional = gameService.getGame(gameId);
        if (gameOptional.isPresent()){
            Game game = gameOptional.get();
            return ResponseEntity.ok(game);  //200 OK + JSON de la partie
        }
        else {
            return ResponseEntity.notFound().build();  //404 Not Found
        }
    }

    //EndPoint 3 : Récupérer les coups possibles pour un jeton
    /**
     * Endpoint REST permettant de récupérer l'ensemble des positions autorisées
     * pour un jeton spécifique au sein d'une partie.
     * <p>
     * Interroge le service pour obtenir les coordonnées accessibles par le jeton
     * identifié par son nom ou son identifiant. Si aucun coup n'est possible ou
     * si la partie / le jeton n'existe pas, un statut 404 est renvoyé.
     * </p>
     *
     * @param gameId l'identifiant unique ({@link UUID}) de la partie transmis dans l'URL.
     * @param tokenId le nom ou l'identifiant du jeton ciblé (ex. "X" ou "0") extrait du chemin d'URL.
     * @return une {@link ResponseEntity} contenant :
     *         <ul>
     *           <li>{@code 200 OK} avec la {@link Collection} des {@link CellPosition} autorisées si au moins un coup est possible.</li>
     *           <li>{@code 404 Not Found} si la partie n'existe pas, si le jeton n'est pas trouvé, ou si aucun coup n'est autorisé.</li>
     *         </ul>
     */
    @GetMapping("/{gameId}/tokens/{tokenId}/moves")
    public ResponseEntity<Collection<CellPosition>> getAllowedMoves(
            @PathVariable UUID gameId,
            @PathVariable String tokenId
    ){
        Collection<CellPosition> moves = gameService.getAllowedMoves(gameId,tokenId);

        /*if (moves.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(moves);*/

        return moves.isEmpty() ? ResponseEntity.notFound().build()
                                : ResponseEntity.ok(moves);

    }

    //EndPoint 4 : Jouer un coup
    /**
     * Endpoint REST permettant de jouer un coup dans une partie en cours.
     * <p>
     * Réceptionne les coordonnées cibles via une requête HTTP POST, délègue
     * l'action au service métier et renvoie l'état mis à jour de la partie.
     * </p>
     *
     * @param gameId l'identifiant unique ({@link UUID}) de la partie transmis dans le chemin d'URL.
     * @param moveParams le corps de la requête JSON ({@link MoveParams}) contenant les coordonnées de destination.
     * @return une {@link ResponseEntity} contenant :
     *         <ul>
     *           <li>{@code 200 OK} avec l'état actualisé du jeu si le coup est validé.</li>
     *           <li>{@code 404 Not Found} si l'identifiant {@code gameId} ne correspond à aucune partie existante.</li>
     *           <li>{@code 400 Bad Request} avec un message d'erreur si la position est invalide,
     *               déjà occupée, ou si aucun coup n'est autorisé.</li>
     *         </ul>
     */
    @PostMapping("/{gameId}/moves")
    public ResponseEntity<?> makeMove(
            @PathVariable UUID gameId,
            @RequestBody MoveParams moveParams
    ){
        try{
            Game updatedGame = gameService.makeMove(gameId,moveParams);
            return ResponseEntity.ok(updatedGame);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();  //404 si la partie n'existe pas
        } catch (InvalidPositionException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));  //400 si coup interdit
        }
    }


}
