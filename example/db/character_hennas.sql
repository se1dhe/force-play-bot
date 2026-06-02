CREATE TABLE IF NOT EXISTS `character_hennas` (
  `char_obj_id` INT NOT NULL DEFAULT 0,
  `symbol_id` INT,
  `slot` INT NOT NULL DEFAULT 0,
  `class_index` SMALLINT(3) NOT NULL DEFAULT 0,
  INDEX (`char_obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;