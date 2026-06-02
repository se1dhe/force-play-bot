DROP TABLE IF EXISTS `buffer_skillsave`;
CREATE TABLE `buffer_skillsave` (
  `charId` int(10) NOT NULL,
  `name` varchar(35) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `skills` varchar(500) DEFAULT '',
  PRIMARY KEY (`charId`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;