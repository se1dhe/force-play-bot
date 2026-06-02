CREATE TABLE `promocode_users` (
  `account_name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  PRIMARY KEY (`account_name`,`code`)
);

