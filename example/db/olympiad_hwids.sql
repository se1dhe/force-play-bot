CREATE TABLE IF NOT EXISTS `olympiad_hwids` (
  `char_id` int(11) NOT NULL DEFAULT '0',
  `hwid` CHAR(64) NOT NULL DEFAULT '',
  `oly_cycle` int(11) NOT NULL DEFAULT '0',
  `free_reset_used` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`char_id`, `hwid`),
  UNIQUE KEY `unique_hwid` (`hwid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
