CREATE TABLE IF NOT EXISTS `character_recipebook` (
  `char_id` INT(10) NOT NULL DEFAULT '0',
  `id` INT(10) NOT NULL DEFAULT '0',
  PRIMARY KEY  (`id`,`char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;