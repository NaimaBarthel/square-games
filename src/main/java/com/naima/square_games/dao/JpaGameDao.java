package com.naima.square_games.dao;

import com.naima.square_games.dao.entities.GameEntity;
import com.naima.square_games.dao.entities.GameTokenEntity;
import com.naima.square_games.dao.repositories.GameEntityRepository;
import com.naima.square_games.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.Token;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Stream;

@Repository
@Primary
public class JpaGameDao implements GameDao{
    private final GameEntityRepository repository;
    private final List<GamePlugin> plugins;

    public JpaGameDao(GameEntityRepository repository, List<GamePlugin> plugins) {
        this.repository = repository;
        this.plugins = plugins;
    }

    @Override
    public Stream<Game> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toGame);

    }
    
    @Override
    public Optional<Game> findById(String gameId){
        return repository.findById(gameId)
                .map(this::toGame);
    }
    
    @Override
    public Game upsert(Game game){
        GameEntity entity = toEntity(game);
        repository.save(entity);
        return game;
    }

    @Override
    public void delete(String gameId) {
        repository.deleteById(gameId);
    }

    private GameEntity toEntity(Game game) {
        GameEntity entity = new GameEntity();
        entity.id = game.getId().toString();
        entity.factoryId = game.getFactoryId();
        entity.boardSize = game.getBoardSize();
        //entity.playerIds = game.getPlayerIds().toString();
        StringJoiner joiner = new StringJoiner(",");
        for (UUID uuid : game.getPlayerIds()) {
            joiner.add(uuid.toString());
        }
        entity.playerIds = joiner.toString(); // Enregistre proprement "uuid1,uuid2"

        //Conversion des jetons du jeu en GAmeTokenEntity
        if (game.getRemainingTokens() != null){
            for (Token token : game.getRemainingTokens()) {
                GameTokenEntity tokenEntity = new GameTokenEntity();
                tokenEntity.name = token.getName();
                if(token.getOwnerId() != null) {
                    tokenEntity.ownerId = token.getOwnerId().toString();
                }

                if (token.getPosition() != null){
                    tokenEntity.x = token.getPosition().x();
                    tokenEntity.y = token.getPosition().y();
                }
                entity.tokens.add(tokenEntity);
            }
        }
        return entity;
    }

    private Game toGame(GameEntity entity) {
        GamePlugin plugin = plugins.stream()
                .filter(p -> p.getId().equalsIgnoreCase(entity.factoryId))
                .findFirst()
                .orElseThrow(()-> new IllegalStateException("Aucun plugin trouvé pour le jeu : " + entity.factoryId));


        List<UUID> players = new ArrayList<>();

        if(entity.playerIds != null && !entity.playerIds.isBlank()) {

            String cleaned = entity.playerIds
                    .replace("[","")
                    .replace("]","")
                    .replace(" ","");

            String[] tabPLayers = cleaned.split(",");
            for (String item : tabPLayers){
                String trimmed = item.trim();

                if( !trimmed.isEmpty()){
                    System.out.println(">>> TENTATIVE UUID: '" + trimmed + "' (longueur: " + trimmed.length() + ")");
                    players.add(UUID.fromString(trimmed));
                }
            }
        }

        //Reconstruit l'instance du jeu via son plugin avec la liste des joueurs
        return plugin.createGame(players, entity.boardSize);
    }

}
