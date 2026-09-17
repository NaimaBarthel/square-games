CREATE TABLE IF NOT EXISTS games (
    id VARCHAR(36) PRIMARY KEY,
    factory_id VARCHAR(255) NOT NULL,
    player_count INT NOT NULL,
    board_size INT NOT NULL
    );