package com.naima.square_games.controllers;

import com.naima.square_games.services.GameCatalog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Locale;

@RestController
public class GameCatalogController {
    //Déclaration de la dépendance en lecture seule
    private final GameCatalog gameCatalog;

    //injection par le constructeur
    public GameCatalogController(GameCatalog gameCatalog) {
        this.gameCatalog = gameCatalog;
    }

    @GetMapping("/catalog")
    public Collection<String> getCatalog(Locale locale) {
        return gameCatalog.getGameNames(locale);
    }



}
