CREATE TABLE IF NOT EXISTS character_skins (
    char_id BIGINT NOT NULL,
    skin_index INT NOT NULL,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (char_id, skin_index)
);