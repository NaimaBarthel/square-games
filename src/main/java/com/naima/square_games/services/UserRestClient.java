package com.naima.square_games.services;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UserRestClient {
    private final RestClient restClient;

    /**
     * Constructeur initialisant l'instance de RestClient dédiée aux échanges
     * avec le microservice "square-users".
     */
    public UserRestClient() {
        //Base URL du microservices users
        //initialise le constructeur fluide (pattern Builder)
        // fourni par Spring Boot 3 pour paramétrer le client HTTP.
        this.restClient = RestClient.builder()
                // .baseUrl(...) : définit le préfixe réseau racine de toutes les requêtes sortantes.
                // Cela évite de répéter le protocole, l'hôte et le port (http://localhost:8081)
                // dans chaque méthode d'appel REST.
                .baseUrl("http://localhost:8081")
                // .build() : compile la configuration et instancie l'objet RestClient immuable.
                .build();
    }

    /**
     * Consomme l'API REST de square-users : GET /users/{id}/valid
     * Renvoie true si l'API retourne 200, false si 404
     */
    public boolean checkUserExists(String userId){
        try{
            return restClient.get()
                    .uri("/users/{id}/valid",userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req,res) -> {
                        //traitement si l'utilisateur n'existe pas (404)
                        System.out.println("Utilisateur non trouvé ou invalide : " + res.getStatusCode());
                    })
                    .toBodilessEntity()
                    .getStatusCode()
                    .is2xxSuccessful();
        }catch (Exception e) {
            return false;
        }

    }



}
