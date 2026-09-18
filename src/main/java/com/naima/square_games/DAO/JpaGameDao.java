package com.naima.square_games.DAO;

import com.naima.square_games.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.Token;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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
        entity.playerIds = game.getPlayerIds().toString();

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
        
        //Reconstruit l'instance du jeu via son plugin
        return plugin.createGame(2, entity.boardSize);
    }

}
