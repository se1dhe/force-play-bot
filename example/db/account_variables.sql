DROP TABLE IF EXISTS `account_variables`;
CREATE TABLE `account_variables` (
  `account_name` varchar(32) NOT NULL DEFAULT '',
  `var` varchar(50) NOT NULL DEFAULT '',
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`account_name`,`var`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;