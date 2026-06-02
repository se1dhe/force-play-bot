DROP TABLE IF EXISTS `trade_ban_hwid`;
CREATE TABLE `trade_ban_hwid` (
  `char_name` VARCHAR(35) CHARACTER SET UTF8 NOT NULL DEFAULT '',
  `hwid` varchar(64) NOT NULL DEFAULT '',
  `ban_time` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`hwid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;