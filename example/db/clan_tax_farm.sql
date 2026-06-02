CREATE TABLE IF NOT EXISTS `clan_tax_farm` (
  `clan_id` INT NOT NULL,
  `item_id` INT NOT NULL,
  `amount` BIGINT NOT NULL,
  PRIMARY KEY (`clan_id`,`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
