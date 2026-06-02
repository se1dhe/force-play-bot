DROP TABLE IF EXISTS `vote_reward`;
CREATE TABLE `vote_reward` (
`account` varchar(32) NOT NULL DEFAULT '',
`hwid` varchar(64) NOT NULL DEFAULT '',
`ip` varchar(15) NOT NULL DEFAULT '',
`time` int(11) NOT NULL DEFAULT '0',
`count` int(11) NOT NULL DEFAULT '0',
`taken` int(11) NOT NULL DEFAULT '0',
`tops` varchar(380) NOT NULL DEFAULT '',
PRIMARY KEY (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;