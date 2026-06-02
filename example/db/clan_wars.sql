CREATE TABLE IF NOT EXISTS `clan_wars` (
  `clan1` INT NOT NULL default 0,
  `clan2` INT NOT NULL default 0,
  `wantspeace1` decimal(2,0) NOT NULL default '0',
  `wantspeace2` decimal(2,0) NOT NULL default '0',
  `type` INT NOT NULL default '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;