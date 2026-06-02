CREATE TABLE IF NOT EXISTS `dropcount` (
  `char_id` INT(10) UNSIGNED NOT NULL,
  `item_id` SMALLINT(5) UNSIGNED NOT NULL,
  `count` BIGINT UNSIGNED default '0',
  UNIQUE KEY `char_id` (`char_id`,`item_id`),
  KEY `char_id_2` (`char_id`)
) ENGINE=MyISAM;