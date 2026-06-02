CREATE TABLE `epic_boss_spawn` (
  `bossId` smallint unsigned NOT NULL,
  `respawnDate` int NOT NULL,
  `state` int NOT NULL,
  `aliveId` int NOT NULL default 0,
  `current_hp` int NOT NULL default 0,
  `current_mp` int NOT NULL default 0,
  `locX` int NOT NULL default 0,
  `locY` int NOT NULL default 0,
  `locZ` int NOT NULL default 0,
  `locH` int NOT NULL default 0,
  PRIMARY KEY  (`bossId`)
) ENGINE=MyISAM;

INSERT INTO `epic_boss_spawn` (`bossId`, `respawnDate`, `state`) VALUES
(29019,'0',0),
(29020,'0',0),
(29028,'0',0),
(29045,'0',0),
(29065,'0',0);