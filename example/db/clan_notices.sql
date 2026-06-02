CREATE TABLE `clan_notices` (
  `clanID` int NOT NULL,
  `notice` varchar(3000) CHARACTER SET UTF8 NOT NULL,
  `enabled` varchar(5) NOT NULL,
  PRIMARY KEY  (`clanID`)
) DEFAULT CHARSET=utf8;