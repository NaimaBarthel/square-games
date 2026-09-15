package com.naima.square_games;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
@RestController
public class GameCatalogController {
    //Déclaration de la dépendance en lecture seule
    private final GameCatalog gameCatalog;

    //injection par le constructeur
    public GameCatalogController(GameCatalog gameCatalog) {
        this.gameCatalog = gameCatalog;
    }

    //Exposition de la ressource en GET  /games
    @GetMapping("/games")
    public Collection<String> getGames(){
        return gameCatalog.getGameIds();
    }



}
