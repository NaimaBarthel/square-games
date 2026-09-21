package com.naima.square_games.DAO;
import com.naima.square_games.DAO.GameDao;

import com.naima.square_games.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
//@Primary  //utiliser en priorité cette interface :la base de données PostgreSQL plutôt que l'interface InMemoryGameDao qui gère la version en mémoire.
public class JdbcGameDao  implements GameDao{

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final List<GamePlugin> plugins;
    private final RowMapper<Game> gameRowMapper;

    /**
     * Constructeur de JdbcGameDao avec injection des dépendances nécessaires par Spring.
     *
     * @param jdbcTemplate Outil Spring configuré pour exécuter les requêtes SQL avec des paramètres nommés (:nomParam).
     * @param plugins      Liste de tous les plugins de jeu enregistrés en tant que composants Spring dans l'application.
     */
    public JdbcGameDao(NamedParameterJdbcTemplate jdbcTemplate, List<GamePlugin> plugins){
        // 1. Affectation des dépendances injectées aux champs de la classe
        this.jdbcTemplate = jdbcTemplate;
        this.plugins = plugins;
        // 2. Initialisation du convertisseur de résultat SQL -> Objet Game
        //    Cette lambda implémente la méthode RowMapper.mapRow(ResultSet rs, int rowNum).
        //    Elle est appelée automatiquement par Spring pour chaque ligne renvoyée par un SELECT.
        this.gameRowMapper = (rs,rowNum) -> {
        // Extraction des données stockées dans les colonnes SQL
        String factoryId = rs.getString("factory_id");  // Nom technique du type de jeu (ex: "tic-tac-toe")
        int playerCount = rs.getInt("player_count");    // Nombre de joueurs configuré
        int boardSize = rs.getInt("board_size");        // Taille du plateau en cases

        // Parcours des plugins disponibles pour trouver la fabrique compatible avec le factory_id de la base
        GamePlugin plugin = this.plugins.stream()
                // On compare sans distinction majuscule/minuscule pour la robustesse
                .filter(p -> p.getId().equalsIgnoreCase(factoryId))
                // On récupère le premier plugin correspondant
                .findFirst()
                // Si la base contient un factory_id orphelin (plugin non présent dans l'application), on lève une exception explicite
                .orElseThrow(() -> new IllegalStateException("Aucun plugin trouvé pour l'identifiant" + factoryId));

        // Délégation de la réinstanciation du moteur de jeu au plugin trouvé
        return plugin.createGame(playerCount,boardSize);

        };
    }


     /**
     * Récupère l'ensemble des parties stockées sous forme de flux.
     *
     * @return un flux contenant toutes les instances de {@link Game}.
     */
    @Override
    public Stream<Game> findAll() {
        String sql = "SELECT id, factory_id, player_count, board_size FROM games";
        return jdbcTemplate.query(sql, gameRowMapper).stream();

    }

    /**
     * Recherche une partie via son identifiant technique unique.
     *
     * @param gameId l'identifiant de la partie.
     * @return un {@link Optional} contenant la partie si trouvée, ou vide sinon.
     */
    @Override
    public Optional<Game> findById(String gameId) {
        String sql = "SELECT id, factory_id, player_count, board_size FROM games WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", gameId);
        try{
            Game game = jdbcTemplate.queryForObject(sql, params, gameRowMapper);
            return Optional.ofNullable(game);
        } catch (EmptyResultDataAccessException e) {
            // Aucun résultat trouvé en base -> on renvoie un Optional vide
            return Optional.empty();
        }
    }

    /**
     * Enregistre ou met à jour l'état d'une partie.
     *
     * @param game la partie à sauvegarder.
     * @return la partie enregistrée.
     */
    @Override
    public Game upsert(Game game) {
        String sql = """
            INSERT INTO games (id, factory_id, player_count, board_size)
            VALUES (:id, :factoryId, :playerCount, :boardSize)
            ON CONFLICT (id) DO UPDATE SET
                factory_id = EXCLUDED.factory_id,
                player_count = EXCLUDED.player_count,
                board_size = EXCLUDED.board_size
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id",game.getId().toString())
                .addValue("factoryId",game.getFactoryId())
                .addValue("playerCount",game.getPlayerIds().size())
                .addValue("boardSize",game.getBoardSize());

        jdbcTemplate.update(sql,params);
        return game;

    }

    /**
     * Supprime une partie à partir de son identifiant.
     *
     * @param gameId l'identifiant de la partie à supprimer.
     */
    @Override
    public void delete(String gameId) {
        String sql = "DELETE FROM games WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", gameId);
        jdbcTemplate.update(sql,params);
    }
}
