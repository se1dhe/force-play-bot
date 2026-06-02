CREATE TABLE IF NOT EXISTS  `account_bonus` (
  `account` varchar(32) NOT NULL,
  `bonus` int NOT NULL,
  `bonus_expire` int(11) NOT NULL,
  PRIMARY KEY (`account`)
);